package com.as.controller;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.as.entity.Monster;
import com.as.repository.MonsterRepository;
import com.as.session.GameSession;

@Controller
@RequestMapping("/games")
public class GameController {

    private final Random random = new Random();
    private final GameSession gameSession;
    private final MonsterRepository monsterRepository;

    private static final int PHASE_STEPS = 10;
    private static final int ACTION_ATTACK = 1;
    private static final int ACTION_MAGIC_ATTACK = 2;
    private static final int ACTION_DEFENCE = 3;
    private static final int ACTION_QUICKNESS = 4;

    public GameController(GameSession gameSession, MonsterRepository monsterRepository) {
        this.gameSession = gameSession;
        this.monsterRepository = monsterRepository;
    }

    @GetMapping("/battle")
    public String battle(Model model, @RequestParam(required = false) Integer actionType,
            @RequestParam(required = false) Long monsterId) {

        addGameSessionToModel(model);

        Monster selectedMonster;
        // 新規モンスターを生成（ただし防御の場合は前回のモンスターを引き継ぐ)
        if (actionType == null || actionType != ACTION_DEFENCE) {
            int phase = (int) Math.floor(gameSession.getWinCount() / PHASE_STEPS) + 1;
            List<Monster> monsters = monsterRepository.findByPhase(phase);
            selectedMonster = drawRandomMonster(monsters);
            model.addAttribute("monster", selectedMonster);
        } else {
            selectedMonster = monsterRepository.findById(monsterId).get(); // FIXME: 空だった時のエラー未対応
            model.addAttribute("monster", selectedMonster);
        }

        // 各行動の確率を抽選
        model.addAttribute("attackRate", calcAttackRate(
                gameSession.getAttack(), selectedMonster.getHp(), selectedMonster.getDefence())); // 自：攻撃、敵：HPと防御
        model.addAttribute("magicAttackRate", calcMagicAttackRate(
                gameSession.getMagicAttack(), selectedMonster.getHp()));  // 自：魔法攻撃：HP
        model.addAttribute("defenceRate", calcDefenceRate(
                gameSession.getHp(), gameSession.getDefence(), selectedMonster.getAttack())); // 自：HPと防御、敵：攻撃
        model.addAttribute("quicknessRate", calcQuicknessRate(
                gameSession.getQuickness(), selectedMonster.getQuickness())); // 自：すばやさ、敵：すばやさ

        return "games/battle";
    }

    @PostMapping("/play")
    public String play(
            @RequestParam int actionType, @RequestParam int actionRate, Model model,
            @RequestParam(required = false) Long monsterId) {
        // 行動したら空腹度増加、逃げる場合は空腹度が-2となる
        gameSession.subtractHungriness();
        if (actionType == ACTION_QUICKNESS) gameSession.subtractHungriness();

        addGameSessionToModel(model);
 
        if (!judge(actionType, actionRate)) return "games/result";

        // 勝利判定をして勝利カウントを増やした上で、空腹度0なら終了
        if (gameSession.getHungriness() <= 0) return "games/result";

        // 防御時はモンスターのリセットをしない
        if (actionType == ACTION_DEFENCE) return "redirect:battle?actionType=" + actionType + "&monsterId=" + monsterId;

        // 逃げる時はボーナスがもらえない
        if (actionType == ACTION_QUICKNESS)  return "redirect:battle";

        gameSession.addWinCount();

        // DEBUG
        if (gameSession.getWinCount() > 30) return "games/result";

        int min = 1;
        int max = 5;
        int baseBonus = new Random().nextInt(max - min + 1) + min;
        int attackBonus, magicAttackBonus, defenceBonus, quicknessBonus;
        attackBonus = magicAttackBonus = defenceBonus = quicknessBonus = baseBonus;

        int hpMin = 1;
        int hpMax = 10;  
        int hpBonus = new Random().nextInt(hpMax - hpMin + 1) + hpMin;
        
        int hungrinessMin = 1;
        int hungrinessMax = 3;
        int hungrinessBonus = new Random().nextInt(hungrinessMax - hungrinessMin + 1) + hungrinessMin;

        model.addAttribute("hpBonus", hpBonus);
        model.addAttribute("attackBonus", attackBonus);
        model.addAttribute("magicAttackBonus", magicAttackBonus);
        model.addAttribute("defenceBonus", defenceBonus);
        model.addAttribute("quicknessBonus", quicknessBonus);
        model.addAttribute("hungrinessBonus", hungrinessBonus);
        return "games/bonus-select";
    }

    @PostMapping("/apply-bonus")
    public String applyBonus(@RequestParam int bonusType, @RequestParam int bonusValue) {
        gameSession.addBonus(bonusType, bonusValue);        
        return "redirect:battle";
    }    

    @GetMapping("/reset")
    public String resetGame() {
        gameSession.reset();
        return "redirect:battle";
    }

    private boolean judge(int actionType, int actionRate) {   
        return this.random.nextInt(100) < actionRate; // 0〜99 の中で 0〜69 は70個 → 70%
    }

    private int calcAttackRate(int attack, int monsterHp, int monsterDefence) {
        double durability = monsterHp + monsterDefence * 2;
        double ratio = durability / (attack * 2);
        // 基本確率を指数関数で滑らかに表現（減少率調整用に基準を2に）、ratio=1→100%, ratio=2→50%
        double baseRate = 100 * Math.pow(0.5, ratio - 1);
        // ランダム誤差を ±5% 加えた上で0~100%にクリッピング
        return (int) Math.round(Math.max(0, Math.min(100, baseRate + ((Math.random() - 0.5) * 10))));
    }

    private int calcMagicAttackRate(int magicAttack, int monsterHp) {
     // 魔法攻撃力が0の時はかなり低めにする
        double ratio = (magicAttack > 0) ? monsterHp / magicAttack : 10;

        // 基本確率を指数関数で滑らかに表現（減少率調整用に基準を2に）、ratio=1→100%, ratio=2→50%
        double baseRate = 100 * Math.pow(0.5, ratio - 1);
        // ランダム誤差を ±5% 加えた上で0~100%にクリッピング
        return (int) Math.round(Math.max(0, Math.min(100, baseRate + ((Math.random() - 0.5) * 10))));
    }

    private int calcDefenceRate(int hp, int defence, int monsterAttack) {
        double durability = hp + defence * 2;
        double ratio = monsterAttack / durability; // 自分が防御側なので分母に耐久性が来る

        // 基本確率を指数関数で滑らかに表現（減少率調整用に基準を2に）、ratio=1→100%, ratio=2→50%
        double baseRate = 100 * Math.pow(0.5, ratio - 1);

        // ランダム誤差を ±5% 加えた上で0~100%にクリッピング
        return (int) Math.round(Math.max(0, Math.min(100, baseRate + ((Math.random() - 0.5) * 10))));
    }

    private int calcQuicknessRate(int quickness, int monsterQuickness) {
        double ratio = monsterQuickness / quickness;

        // 基本確率を指数関数で滑らかに表現（減少率調整用に基準を2に）、ratio=1→100%, ratio=2→50%
        double baseRate = 100 * Math.pow(0.5, ratio - 1);

        // ランダム誤差を ±5% 加えた上で0~100%にクリッピング
        return (int) Math.round(Math.max(0, Math.min(100, baseRate + ((Math.random() - 0.5) * 10))));
    }

    // DEBUG: ChatGPTに聞いたのであまり理解していないアルゴリズム
    private Monster drawRandomMonster(List<Monster> monsters) {
        int totalWeight = monsters.stream().mapToInt(Monster::getIncidence).sum();
        int randomValue = new Random().nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (Monster monster : monsters) {
            cumulativeWeight += monster.getIncidence();
            if (randomValue < cumulativeWeight) {
                return monster;
            }
        }

        return monsters.get(0); // 安全のため（実行されることは基本的にない）
    }

    // WARNING：使うタイミングによってはmodelに2回目のaddをしているものがある
    private void addGameSessionToModel(Model model) {
        model.addAttribute("winCount", gameSession.getWinCount());
        model.addAttribute("hp", gameSession.getHp());
        model.addAttribute("attack", gameSession.getAttack());
        model.addAttribute("magicAttack", gameSession.getMagicAttack());
        model.addAttribute("defence", gameSession.getDefence());
        model.addAttribute("quickness", gameSession.getQuickness());
        model.addAttribute("hungriness", gameSession.getHungriness());
    }
}