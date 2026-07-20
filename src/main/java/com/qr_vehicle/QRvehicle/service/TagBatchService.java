package com.qr_vehicle.QRvehicle.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.qr_vehicle.QRvehicle.entity.TagBatch;
import com.qr_vehicle.QRvehicle.entity.TagInventory;
import com.qr_vehicle.QRvehicle.entity.TagSheet;
import com.qr_vehicle.QRvehicle.repository.TagBatchRepository;
import com.qr_vehicle.QRvehicle.repository.TagInventoryRepository;
import com.qr_vehicle.QRvehicle.util.BatchCodeGenerator;
import com.qr_vehicle.QRvehicle.util.StickerSheetGenerator;

import jakarta.transaction.Transactional;

@Service
public class TagBatchService {

    private final TagBatchRepository batchRepository;
    private final TagInventoryService inventoryService;
    private final TagInventoryRepository tagInventoryRepository;
    private final TagSheetService tagSheetService;

    public TagBatchService(
            TagBatchRepository batchRepository,
            TagInventoryRepository tagInventoryRepository,
            TagInventoryService inventoryService,
            TagSheetService tagSheetService) {

        this.batchRepository = batchRepository;
        this.tagInventoryRepository = tagInventoryRepository;
        this.inventoryService = inventoryService;
        this.tagSheetService = tagSheetService;
    }

    public TagBatch save(TagBatch batch) {
        return batchRepository.save(batch);
    }

    public List<TagBatch> getAll() {
        return batchRepository.findAll();
    }

    public TagBatch getById(Long id) {
        return batchRepository.findById(id).orElse(null);
    }

    public TagBatch getByBatchCode(String batchCode) {
        return batchRepository.findByBatchCode(batchCode).orElse(null);
    }

    public void delete(Long id) {
        batchRepository.deleteById(id);
    }

    public byte[] generateSheetPdf(String sheetCode) throws Exception {

        System.out.println("Loading Sheet : " + sheetCode);

        List<TagInventory> tags =
                tagInventoryRepository.findBySheetCode(sheetCode);

        System.out.println("Tags Found : " + tags.size());

        return StickerSheetGenerator.generate(tags);
    }

    

    public byte[] generateBatchPdf(String batchCode) throws Exception {

        System.out.println("Loading Batch : " + batchCode);

        List<TagInventory> inventory =
                tagInventoryRepository.findByBatchCode(batchCode);

        System.out.println("Inventory Found : " + inventory.size());

        return StickerSheetGenerator.generate(inventory);
    }

    @Transactional
    public TagBatch generateBatch(String stickerType, Integer vehicleCount) {

        System.out.println("==================================");
        System.out.println("START GENERATE BATCH");
        System.out.println("Sticker Type : " + stickerType);
        System.out.println("Vehicle Count : " + vehicleCount);

        final int VEHICLES_PER_SHEET = 10;

        long totalBatch = batchRepository.count() + 1;

        System.out.println("Existing Batch Count : " + (totalBatch - 1));

        String batchCode =
                BatchCodeGenerator.generate(
                        stickerType,
                        totalBatch);

        System.out.println("Generated Batch Code : " + batchCode);

        TagBatch batch = new TagBatch();

        batch.setBatchCode(batchCode);
        batch.setStickerType(stickerType);
        batch.setVehiclesCount(vehicleCount);
        batch.setStickersCount(vehicleCount * 2);
        batch.setPrinted(false);

        batch = batchRepository.save(batch);

        System.out.println("Batch Saved");

        int totalSheets =
                (int) Math.ceil((double) vehicleCount / VEHICLES_PER_SHEET);

        System.out.println("Total Sheets : " + totalSheets);

        int remainingVehicles = vehicleCount;

        for (int sheet = 1; sheet <= totalSheets; sheet++) {

            System.out.println("--------------------------------");

            System.out.println("Creating Sheet : " + sheet);

            int vehiclesThisSheet =
                    Math.min(
                            remainingVehicles,
                            VEHICLES_PER_SHEET);

            System.out.println("Vehicles In Sheet : " + vehiclesThisSheet);

            String sheetCode =
                    batchCode + "-S" + String.format("%02d", sheet);

            System.out.println("Sheet Code : " + sheetCode);

            TagSheet tagSheet = new TagSheet();

            tagSheet.setBatchCode(batchCode);
            tagSheet.setSheetCode(sheetCode);
            tagSheet.setSheetNumber(sheet);
            tagSheet.setVehiclesCount(vehiclesThisSheet);
            tagSheet.setStickersCount(vehiclesThisSheet * 2);

            tagSheetService.save(tagSheet);

            System.out.println("Sheet Saved");

            generateSheetInventory(
                    batchCode,
                    sheetCode,
                    sheet,
                    stickerType,
                    vehiclesThisSheet);

            remainingVehicles -= vehiclesThisSheet;

            System.out.println("Remaining Vehicles : " + remainingVehicles);
        }

        System.out.println("BATCH COMPLETED");
        System.out.println("==================================");

        return batch;
    }

    private void generateSheetInventory(
            String batchCode,
            String sheetCode,
            int sheetNumber,
            String stickerType,
            int vehicles) {

        System.out.println("Generating Inventory");

        int pair = 1;

        for (int row = 1; row <= 5; row++) {

            for (int col = 1; col <= 4; col += 2) {

                if (pair > vehicles) {

                    System.out.println("Inventory Completed");

                    return;
                }

                System.out.println("----------------------");
                System.out.println("Pair : " + pair);
                System.out.println("Row : " + row);
                System.out.println("Column : " + col);

                TagInventory inventory = new TagInventory();

                inventory.setBatchCode(batchCode);
                inventory.setSheetCode(sheetCode);
                inventory.setSheetNumber(sheetNumber);

                inventory.setStickerType(stickerType);

                String tagId =
                        inventoryService.generateRandomTagId();

                System.out.println("Generated Tag ID : " + tagId);

                inventory.setTagId(tagId);

                String uniqueCode =
                        inventoryService.generateUniqueCode();

                System.out.println("Generated UUID : " + uniqueCode);

                inventory.setUniqueCode(uniqueCode);

                inventory.setAssigned(false);

                inventory.setSheetRow(row);
                inventory.setSheetColumn(col);
                inventory.setPairIndex(pair);

                inventoryService.save(inventory);

                System.out.println("Inventory Saved");

                pair++;
            }
        }
    }
}