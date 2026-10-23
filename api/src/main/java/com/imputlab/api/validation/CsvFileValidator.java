package com.imputlab.api.validation;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class CsvFileValidator {

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "CSV file must not be empty."
            );
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null
                || !fileName.toLowerCase(java.util.Locale.ROOT)
                        .endsWith(".csv")) {
            throw new IllegalArgumentException(
                    "File must have a .csv extension."
            );
        }
    }
}
