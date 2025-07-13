package ru.inno.course.player.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.inno.course.player.model.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

public class DataProviderJSON implements DataProvider{
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path filePath;
    @Override
    public void save(Collection<Player> players) throws IOException {
        mapper.writeValue(filePath.toFile(), players);
    }
    // Существующий конструктор для совместимости
    public DataProviderJSON() {
        this.filePath = Path.of("./data.json");
    }

    // Новый конструктор для указания пути
    public DataProviderJSON(String filePath) {
        this.filePath = Path.of(filePath);
    }

    @Override
    public Collection<Player> load() throws IOException {
        File file = filePath.toFile();
        // Проверяем существование файла перед чтением
        if (!file.exists() || file.length() == 0) {
            return Collections.emptyList();
        }
        return mapper.readValue(filePath.toFile(), new TypeReference<Collection<Player>>(){});
    }
}
