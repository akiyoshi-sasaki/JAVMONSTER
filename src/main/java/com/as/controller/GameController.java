package com.as.controller;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.as.session.GameSession;

@Controller
@RequestMapping("/games")
public class GameController {
    private final GameSession gameSession;
    private final Random random = new Random();

    public GameController(GameSession gameSession) {
        this.gameSession = gameSession;
    }

    @GetMapping("/battle")
    public String battle(Model model) {
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

        // 新規モンスターを生成（ただし防御の場合は前回のモンスターを引き継ぐ)

        // 各行動の確率を抽選
        // DEBUG: モンスターのステータスは一旦固定値
        model.addAttribute("attackRate", calcAttackRate(attack, 45, 8)); // HPと防御
        model.addAttribute("magicAttackRate", calcMagicAttackRate(magicAttack, 45)); // HPのみ
        model.addAttribute("defenceRate", calcDefenceRate(defence, 7)); // 攻撃
        model.addAttribute("quicknessRate", calcQuicknessRate(quickness, 5)); // すばやさ

        return "games/battle";
    }

    @PostMapping("/play")
    public String play(@RequestParam int actionType, @RequestParam int actionRate, Model model) {

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
            return "redirect:battle";
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
}