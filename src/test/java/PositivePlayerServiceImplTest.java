import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.inno.course.player.data.DataProviderJSON;
import ru.inno.course.player.model.Player;
import ru.inno.course.player.service.PlayerServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositivePlayerServiceImplTest {
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
    @DisplayName("Добавить игрока - проверить наличие в списке")
    void testCreatePlayerAndCheckPresence() {

        int playerId = playerService.createPlayer("testPlayer");
        Player player = playerService.getPlayerById(playerId);

        assertNotNull(player);
        assertEquals("testPlayer", player.getNick());
        assertEquals(0, player.getPoints());
        assertTrue(player.isOnline());
    }

    @Test
    @DisplayName("(добавить игрока) - удалить игрока - проверить отсутствие в списке")
    void testDeletePlayerAndCheckAbsence() {

        int playerId = playerService.createPlayer("testPlayer");
        Player deletedPlayer = playerService.deletePlayer(playerId);

        assertNotNull(deletedPlayer);
        assertThrows(NoSuchElementException.class, () -> playerService.getPlayerById(playerId));
    }

    @Test
    @DisplayName("(нет json-файла) добавить игрока")
    void testCreatePlayerWithoutJsonFile() {

        int playerId = playerService.createPlayer("newPlayer");
        assertTrue(playerId > 0);
    }

    @Test
    @DisplayName("(есть json-файл) добавить игрока")
    void testAddSecondPlayerAndCheckPresence() {

        int playerFirstId = playerService.createPlayer("testPlayer1");
        int playerSecondId = playerService.createPlayer("testPlayer2");
        Player firstPlayer = playerService.getPlayerById(playerFirstId);
        Player secondPlayer = playerService.getPlayerById(playerSecondId);

        assertNotNull(firstPlayer);
        assertNotNull(secondPlayer);
        assertEquals("testPlayer1", firstPlayer.getNick());
        assertEquals("testPlayer2", secondPlayer.getNick());
        assertEquals(0, firstPlayer.getPoints());
        assertEquals(0, secondPlayer.getPoints());
        assertTrue(firstPlayer.isOnline());
        assertTrue(secondPlayer.isOnline());
    }

    @Test
    @DisplayName("Начислить баллы существующему игроку")
    void testAddPointsToNewPlayer() {
        playerService.createPlayer("newTestPlayer");
        playerService.addPoints(1, 10);
    }

    @Test
    @DisplayName("Добавить очков поверх существующих")
    void testAddPointsToExistingPlayer() {
        playerService.createPlayer("testPlayer");
        playerService.addPoints(1,10);
        playerService.initStorages();

        int newPoints = playerService.addPoints(1, 5);
        assertEquals(15, newPoints);
    }

    @Test
    @DisplayName("(добавить игрока) - получить игрока по id")
    void testGetPlayerByIdAfterCreation() {

        int playerId = playerService.createPlayer("testPlayer");
        Player player = playerService.getPlayerById(playerId);

        assertEquals(playerId, player.getId());
    }

    @Test
    @DisplayName("Проверить корректность сохранения в файл; " +
            "Проверить корректность загрузки json-файла")
    void testPlayerListSavedToFileCorrectly(@TempDir Path tempDir) throws Exception {
        // 1. Создаем временный файл
        Path testFile = tempDir.resolve("test-data.json");

        // 2. Создаем провайдер с временным файлом
        DataProviderJSON provider = new DataProviderJSON(testFile.toString());

        // 3. Создаем сервис с нашим тестовым провайдером
        PlayerServiceImpl service = new PlayerServiceImpl(provider);

        // 4. Добавляем тестовых игроков
        int playerId1 = service.createPlayer("player1");
        int playerId2 = service.createPlayer("player2");
        service.addPoints(playerId1, 10);
        service.addPoints(playerId2, 20);

        // 5. Проверяем, что файл создан
        assertTrue(Files.exists(testFile), "Файл должен быть создан");
        assertTrue(Files.size(testFile) > 0, "Файл не должен быть пустым");

        // 6. Загружаем данные обратно из файла
        Collection<Player> savedPlayers = provider.load();

        // 7. Проверяем корректность сохраненных данных
        assertEquals(2, savedPlayers.size(), "Должно быть 2 игрока в файле");

        Player player1 = findPlayerById(savedPlayers, playerId1);
        assertNotNull(player1, "Игрок 1 должен присутствовать");
        assertEquals("player1", player1.getNick());
        assertEquals(10, player1.getPoints());
        assertTrue(player1.isOnline());

        Player player2 = findPlayerById(savedPlayers, playerId2);
        assertNotNull(player2, "Игрок 2 должен присутствовать");
        assertEquals("player2", player2.getNick());
        assertEquals(20, player2.getPoints());
        assertTrue(player2.isOnline());
    }

    private Player findPlayerById(Collection<Player> players, int id) {
        return players.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("Проверить, что id всегда уникальный")
    void testUniqueIdAfterDeletion() {

        // Создаем 5 игроков
        for (int i = 1; i <= 5; i++) {
            playerService.createPlayer("player" + i);
        }

        // Удаляем 3-го игрока
        playerService.deletePlayer(3);

        // Создаем нового игрока
        int newPlayerId = playerService.createPlayer("newPlayer");

        assertEquals(6, newPlayerId);
    }

    @Test
    @DisplayName("(нет json-файла) запросить список игроков")
    void testGetPlayerWithoutJsonFile() {

        int playerId = playerService.createPlayer("newPlayer");
        assertNotNull(playerService.getPlayerById(playerId));
    }

    @Test
    @DisplayName("Проверить создание игрока с 15 символами")
    void testCreatePlayerWith15CharacterNickname() {

        String longNickname = "a".repeat(15);
        int playerId = playerService.createPlayer(longNickname);

        Player player = playerService.getPlayerById(playerId);
        assertEquals(longNickname, player.getNick());
    }
}
