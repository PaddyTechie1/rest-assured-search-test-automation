package org.search.api.usecase.helper;

import org.search.api.usecase.util.ParameterConstants;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

public final class LoggerHelper {

    private LoggerHelper() {
    }

    public static void log(final Logger logger, final String fileName, final String message) {
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Paths.get(fileName).toAbsolutePath(), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND), true)) {
            out.write(logger.getName() + " : " + message + "\n");
        } catch (final IOException ioException) {
            logger.error(ioException.getMessage(), ioException.getStackTrace());
        }
    }

    public static String getFileToWrite(final String vendorName) {
        return ParameterConstants.LOG_FILE_PATH_PREFIX + ParameterConstants.UNDERSCORE + vendorName + ParameterConstants.UNDERSCORE + new Date().getTime() + ParameterConstants.LOG_FILE_EXTENSION;
    }
}
