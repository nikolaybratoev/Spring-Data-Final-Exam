package softuni.exam.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileIOUtilImpl implements FileIOUtil {
    @Override
    public String readFileContent(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath))
                .stream()
                .filter(row -> !row.isEmpty())
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
