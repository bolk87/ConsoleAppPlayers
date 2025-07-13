import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import ru.inno.course.player.data.DataProviderJSON;
import ru.inno.course.player.service.PlayerServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class NegativePlayerServiceImplTest {
    private PlayerServiceImpl playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerServiceImpl();
    }

    @AfterEach
    public void deleteFile() throws IOException {
        Files.deleteIfExists(Path.of("./data.json"));
    }

    @Test
    @DisplayName("Удалить игрока, которого нет")
    void testDeleteNonExistentPlayer() {

        assertThrows(NoSuchElementException.class, () -> playerService.deletePlayer(10));
    }

    @Test
    @DisplayName("Создать дубликат (имя уже занято)")
    void testCreateDuplicateNickname() {

        playerService.createPlayer("testPlayer");
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> playerService.createPlayer("testPlayer"));
        // Проверяем текст сообщения
        String expectedMessage = "Nickname is already in use: testPlayer";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Получить игрока по id, которого нет")
    void testGetNonExistentPlayer() {

        assertThrows(NoSuchElementException.class, () -> playerService.getPlayerById(1));
    }

    //Тест ожидает, что нельзя создать пользователя с пустым именем, но код приложения позволяет это сделать
    @Test
    @DisplayName("Сохранить игрока с пустым ником")
    void testCreatePlayerWithEmptyNickname() {
        assertThrows(IllegalArgumentException.class, () -> playerService.createPlayer(""));
    }

    //Тест падает, так как ожидает ошибку, но ошибка не возвращается
    @Test
    @DisplayName("Начислить отрицательное число очков")
    void testAddNegativePoints() {
        playerService.createPlayer("testPlayer");
        playerService.addPoints(1, 7);

        assertThrows(IllegalArgumentException.class, () -> playerService.addPoints(1, -5));
    }

    @Test
    @DisplayName("Накинуть очков игроку, которого нет")
    void testAddPointsToNonExistentPlayer() {
        assertThrows(NoSuchElementException.class, () -> playerService.addPoints(1, 5));
    }

    // Java не позволяет написать метод без указания номера ид. Поэтому я использую отрицательный номер
    @Test
    @DisplayName("Накинуть очков без указания id")
    void testAddPointsWithoutId() {
        assertThrows(NoSuchElementException.class, () -> playerService.addPoints(-1, 9));
    }

    //Тест 8. Ввести невалидный id (String)
    // Тест не может быть написан, так как Java не позволяет передать параметр с типом String, если указано int

    //10. Начислить 1.5 балла игроку
    // Тест не может быть написан, так как Java не позволяет передать параметр с типом float, если указано int

    @Test
    @DisplayName("Проверить корректность загрузки json-файла с дубликатами")
    void testPlayerDupleSavedToFile(@TempDir Path tempDir) throws Exception {
        // 1. Создаем временный файл
        Path testFile = tempDir.resolve("test-data-duple.json");

        // 2. Создаем провайдер с временным файлом
        DataProviderJSON provider = new DataProviderJSON(testFile.toString());

        // 3. Создаем сервис с нашим тестовым провайдером
        PlayerServiceImpl service = new PlayerServiceImpl(provider);

        // 4. Добавляем тестовых игроков
        int playerId1 = service.createPlayer("player1");
        service.addPoints(playerId1, 10);
        //пробуем создать игрока дубликат, для записи в файл
        Exception exception = assertThrows(IllegalArgumentException.class,
                () ->  service.createPlayer("player1"));
        // 5. Проверяем, что второй игрок не создан и возвращается ошибка
        String expectedMessage = "Nickname is already in use: player1";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    //Тест ожидает, что нельзя создать пользователя с длинным именем, но код приложения позволяет это сделать
    @Test
    @DisplayName("Проверить создание игрока с 16 символам")
    void testCreatePlayerWith16CharacterNickname() {

        String tooLongNickname = "a".repeat(16);
        assertThrows(IllegalArgumentException.class, () -> playerService.createPlayer(tooLongNickname));
    }
}