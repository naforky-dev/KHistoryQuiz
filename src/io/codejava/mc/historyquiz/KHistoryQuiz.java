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

public class KHistoryQuiz extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private BukkitTask quizTask;
    private boolean isRunning = false;
    private int questionIndex = 0;

    @Override
    public void onEnable() {
        // 설정 파일 생성
        saveDefaultConfig();
        
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
            player.sendMessage("사용법: /khistoryquiz <start|pause|stop|loaddefaults>");
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
            default:
                player.sendMessage("알 수 없는 명령어입니다. /khistoryquiz <start|pause|stop|loaddefaults>");
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("start", "pause", "stop", "reset", "loaddefaults");
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

        player.sendMessage("[KHistoryQuiz] 퀴즈가 시작되었습니다. (간격: " + delayMs + "ms)");
        getLogger().info("[KHistoryQuiz] 퀴즈 타이머 시작됨.");
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
        player.sendMessage("[KHistoryQuiz] 기본 설정이 로드되었습니다.");
        getLogger().info("[KHistoryQuiz] 기본 설정 불러오기 완료.");
    }

    private void broadcastQuestion() {
        // 문제 전송 로직 (향후 구현)
        Bukkit.broadcastMessage("[KHistoryQuiz] 문제 #" + (questionIndex + 1));
        questionIndex++;
    }
}