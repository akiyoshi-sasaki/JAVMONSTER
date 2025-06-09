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
    
    public GameController(GameSession gameSession, MonsterRepository monsterRepository) {
        this.gameSession = gameSession;
        this.monsterRepository = monsterRepository;
    }

    @GetMapping("/battle")
    public String battle(Model model, @RequestParam(required = false) Integer actionType,
            @RequestParam(required = false) Long monsterId) {
        int attack = gameSession.getAttack();
        int magicAttack = gameSession.getMagicAttack();
        int defence = gameSession.getDefence();
        int quickness = gameSession.getQuickness();

        model.addAttribute("hp", gameSession.getHp());
        model.addAttribute("attack", attack);
        model.addAttribute("magicAttack", magicAttack);
        model.addAttribute("defence", defence);
        model.addAttribute("quickness", quickness);
        model.addAttribute("hungriness", gameSession.getHungriness());
        model.addAttribute("winCount", gameSession.getWinCount());

        Monster selectedMonster;
        // 新規モンスターを生成（ただし防御の場合は前回のモンスターを引き継ぐ)
        if (actionType == null || actionType != 3) {
            List<Monster> monsters = monsterRepository.findByPhase(1); // DEBUG：phaseを固定
            selectedMonster = drawRandomMonster(monsters);
            model.addAttribute("monster", selectedMonster);
        } else {
            selectedMonster = monsterRepository.findById(monsterId).get(); // FIXME: 空だった時のエラー未対応
            model.addAttribute("monster", selectedMonster);
        }
        
        // 各行動の確率を抽選
        model.addAttribute("attackRate", calcAttackRate(attack, selectedMonster.getHp(), selectedMonster.getDefence())); // HPと防御
        model.addAttribute("magicAttackRate", calcMagicAttackRate(magicAttack, selectedMonster.getHp())); // HPのみ
        model.addAttribute("defenceRate", calcDefenceRate(defence, selectedMonster.getAttack())); // 攻撃
        model.addAttribute("quicknessRate", calcQuicknessRate(quickness, selectedMonster.getQuickness())); // すばやさ

        return "games/battle";
    }

    @PostMapping("/play")
    public String play(
            @RequestParam int actionType, @RequestParam int actionRate, Model model,
            @RequestParam(required = false) Long monsterId) {
        gameSession.subtractHungriness();
        if (actionType == 4) {
            // 逃げる場合は空腹度-2となる
            gameSession.subtractHungriness();
        }

        model.addAttribute("winCount", gameSession.getWinCount());
        model.addAttribute("hp", gameSession.getHp());
        model.addAttribute("attack", gameSession.getAttack());
        model.addAttribute("magicAttack", gameSession.getMagicAttack());
        model.addAttribute("defence", gameSession.getDefence());
        model.addAttribute("quickness", gameSession.getQuickness());
        model.addAttribute("hungriness", gameSession.getHungriness());

        if (!judge(actionType, actionRate)) {
            return "games/result";
        }

        if (actionType == 3) {
            // モンスターのリセットをしない
            return "redirect:battle?actionType=" + actionType + "&monsterId=" + monsterId;
        }

        if (actionType == 4) {
            // ボーナスが貰えない
            return "redirect:battle";
        }

        gameSession.addWinCount();

        // DEBUG
        if (gameSession.getWinCount() > 10) {
            return "games/result";
        }
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
        return Math.round((attack * attack) - (monsterDefence * monsterHp / 10));
    }

    private int calcMagicAttackRate(int magicAttack, int monsterHp) {
        return Math.round((magicAttack * magicAttack) - (monsterHp / 10));
    }

    private int calcDefenceRate(int attack, int monsterAttack) {
        return Math.round((attack * attack) - (monsterAttack * monsterAttack));
    }

    private int calcQuicknessRate(int quickness, int monsterQuickness) {
        return Math.round((quickness * quickness) - (monsterQuickness * monsterQuickness));
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
}