package io.codejava.mc.historyquiz;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public class KHistoryQuiz extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private BukkitTask quizTask;
    private boolean isRunning = false;
    private int questionIndex = 0;
    private FileConfiguration questionsConfig;
    private List<String> questionIds;
    private int questionTimeout = 10; // default: 10 seconds

    @Override
    public void onEnable() {
        // 설정 파일 생성
        saveDefaultConfig();
        loadQuestionsConfig();
        loadQuestionTimeout();
        
        getLogger().info("[KHistoryQuiz] 명령어 실행기 등록 중...");
        getCommand("khistoryquiz").setExecutor(this);
        getCommand("khistoryquiz").setTabCompleter(this);
        
        getLogger().info("[KHistoryQuiz] v1.0 활성화됨.");
    }

    @Override
    public void onDisable() {
        if (quizTask != null) {
            quizTask.cancel();
        }
        getLogger().info("[KHistoryQuiz] 플러그인 비활성화됨.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("오류: 이 명령어는 플레이어만 실행할 수 있습니다.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("사용법: /khistoryquiz <start|pause|stop|loaddefaults|set>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start":
                startQuiz(player);
                return true;
            case "pause":
                pauseQuiz(player);
                return true;
            case "stop":
            case "reset":
                stopQuiz(player);
                return true;
            case "loaddefaults":
                loadDefaults(player);
                return true;
            case "set":
                setConfigValue(player, args);
                return true;
            default:
                player.sendMessage("알 수 없는 명령어입니다. /khistoryquiz <start|pause|stop|loaddefaults|set>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("start", "pause", "stop", "reset", "loaddefaults", "set");
        }
        if (args.length == 2 && "set".equalsIgnoreCase(args[0])) {
            return List.of("interval", "question-timeout");
        }
        return new ArrayList<>();
    }

    private void startQuiz(Player player) {
        if (isRunning) {
            player.sendMessage("[KHistoryQuiz] 이미 퀴즈가 실행 중입니다.");
            return;
        }

        isRunning = true;
        questionIndex = 0;
        String interval = getConfig().getString("interval", "240000");
        long delayMs = 0;

        try {
            if ("random".equalsIgnoreCase(interval)) {
                delayMs = 240000; // 기본값: 4분
            } else {
                delayMs = Long.parseLong(interval);
            }
        } catch (NumberFormatException e) {
            delayMs = 240000;
            getLogger().warning("[KHistoryQuiz] 유효하지 않은 interval 값. 기본값(240000ms)을 사용합니다.");
        }

        long delayTicks = delayMs / 50; // 1 tick = 50ms

        quizTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            broadcastQuestion();
        }, delayTicks, delayTicks);

        player.sendMessage("[KHistoryQuiz] 퀴즈가 시작되었습니다. (간격: " + delayMs + "ms, 제한시간: " + questionTimeout + "초)");
        getLogger().info("[KHistoryQuiz] 퀴즈 타이머 시작됨. (question-timeout: " + questionTimeout + "초)");
    }

    private void pauseQuiz(Player player) {
        if (quizTask == null || !isRunning) {
            player.sendMessage("[KHistoryQuiz] 실행 중인 퀴즈가 없습니다.");
            return;
        }

        quizTask.cancel();
        isRunning = false;
        player.sendMessage("[KHistoryQuiz] 퀴즈가 일시정지되었습니다.");
        getLogger().info("[KHistoryQuiz] 퀴즈 일시정지됨.");
    }

    private void stopQuiz(Player player) {
        if (quizTask == null) {
            player.sendMessage("[KHistoryQuiz] 실행 중인 퀴즈가 없습니다.");
            return;
        }

        quizTask.cancel();
        quizTask = null;
        isRunning = false;
        questionIndex = 0;
        player.sendMessage("[KHistoryQuiz] 퀴즈가 정지되고 리셋되었습니다.");
        getLogger().info("[KHistoryQuiz] 퀴즈 정지 및 리셋됨.");
    }

    private void loadDefaults(Player player) {
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        loadQuestionsConfig();
        loadQuestionTimeout();
        player.sendMessage("[KHistoryQuiz] 기본 설정이 로드되었습니다.");
        getLogger().info("[KHistoryQuiz] 기본 설정 불러오기 완료.");
    }

    private void setConfigValue(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage("사용법: /khistoryquiz set <key> <value>");
            player.sendMessage("가능한 키: interval, question-timeout");
            return;
        }

        String key = args[1].toLowerCase();
        String value = args[2];

        switch (key) {
            case "interval":
                try {
                    long interval = Long.parseLong(value);
                    if (interval <= 0) {
                        player.sendMessage("§c오류: interval은 0보다 커야 합니다.");
                        return;
                    }
                    getConfig().set("interval", interval);
                    player.sendMessage("§a설정 변경 완료: interval = " + interval + "ms");
                    getLogger().info("[KHistoryQuiz] Interval set to " + interval + "ms (not saved to config.yml)");
                } catch (NumberFormatException e) {
                    player.sendMessage("§c오류: interval은 숫자여야 합니다.");
                }
                return;

            case "question-timeout":
                try {
                    int timeout = Integer.parseInt(value);
                    if (timeout <= 0) {
                        player.sendMessage("§c오류: question-timeout은 0보다 커야 합니다.");
                        return;
                    }
                    questionTimeout = timeout;
                    getConfig().set("question-timeout", timeout);
                    player.sendMessage("§a설정 변경 완료: question-timeout = " + timeout + "초");
                    getLogger().info("[KHistoryQuiz] Question timeout set to " + timeout + "s (not saved to config.yml)");
                } catch (NumberFormatException e) {
                    player.sendMessage("§c오류: question-timeout은 숫자여야 합니다.");
                }
                return;

            default:
                player.sendMessage("§c알 수 없는 키: " + key);
                player.sendMessage("가능한 키: interval, question-timeout");
        }
    }

    private void broadcastQuestion() {
        if (questionsConfig == null || questionIds == null || questionIds.isEmpty()) {
            Bukkit.broadcastMessage("§c[KHistoryQuiz] Error: Could not load questions.");
            getLogger().warning("[KHistoryQuiz] Question loading failed");
            return;
        }

        // Reset index if it exceeds the size
        if (questionIndex >= questionIds.size()) {
            questionIndex = 0;
        }

        String questionId = questionIds.get(questionIndex);
        String question = questionsConfig.getString("questions." + questionId + ".question");
        String answer = questionsConfig.getString("questions." + questionId + ".answer");

        if (question == null || answer == null) {
            Bukkit.broadcastMessage("§c[KHistoryQuiz] Error: Question not found.");
            getLogger().warning("[KHistoryQuiz] Question #" + (questionIndex + 1) + " loading failed");
            questionIndex++;
            return;
        }

        // Display dialog-style message to all players
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Display the question as a structured dialog-like message
            player.sendMessage("§6§m" + "━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§6§l문제 #" + (questionIndex + 1));
            player.sendMessage("§6§m" + "━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§e" + question);
            player.sendMessage("§6§m" + "━━━━━━━━━━━━━━━━━━━");
            player.sendMessage("§7정답 입력:");
            player.sendMessage("§b[보기]");
            player.sendMessage(" ");
        }

        // Also broadcast to server console
        Bukkit.broadcastMessage("§6§l[KHistoryQuiz] Question #" + (questionIndex + 1) + ": " + question);
        getLogger().info("[KHistoryQuiz] Question #" + (questionIndex + 1) + ": " + question + " (Answer: " + answer + ")");
        questionIndex++;
    }

    private void loadQuestionsConfig() {
        File questionsFile = new File(getDataFolder(), "questions.yml");
        
        if (!questionsFile.exists()) {
            getLogger().warning("[KHistoryQuiz] questions.yml을 찾을 수 없습니다. 기본 파일을 생성합니다.");
            saveResource("questions.yml", false);
        }

        questionsConfig = YamlConfiguration.loadConfiguration(questionsFile);
        
        // 질문 아이디 목록 로드
        if (questionsConfig.contains("questions")) {
            questionIds = new ArrayList<>(questionsConfig.getConfigurationSection("questions").getKeys(false));
            getLogger().info("[KHistoryQuiz] " + questionIds.size() + "개의 질문이 로드되었습니다.");
        } else {
            questionIds = new ArrayList<>();
            getLogger().warning("[KHistoryQuiz] questions.yml에 질문이 없습니다.");
        }
    }

    private void loadQuestionTimeout() {
        questionTimeout = getConfig().getInt("question-timeout", 10);
        
        if (questionTimeout <= 0) {
            questionTimeout = 10;
            getLogger().warning("[KHistoryQuiz] 유효하지 않은 question-timeout 값. 기본값(10초)을 사용합니다.");
        } else {
            getLogger().info("[KHistoryQuiz] Question timeout loaded: " + questionTimeout + " seconds");
        }
    }
}