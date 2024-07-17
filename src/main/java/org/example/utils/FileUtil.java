package org.example.utils;

import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.example.products.AsosProduct;
import org.example.products.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileUtil {

    public static void writeToCsvFile(List<Product> products) {

        try (FileWriter writer = new FileWriter("products.csv", false)) {
            var builder = new StatefulBeanToCsvBuilder<Product>(writer)
                    .withQuotechar(CSVWriter.DEFAULT_QUOTE_CHARACTER)
                    .withSeparator(',')
                    .build();

            builder.write(products);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CsvRequiredFieldEmptyException e) {
            throw new RuntimeException(e);
        } catch (CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        }
    }
}
