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

        model.addAttribute("hp", gameSession.getHp());
        model.addAttribute("attack", gameSession.getAttack());
        model.addAttribute("magic_attack", gameSession.getMagicAttack());
        model.addAttribute("defence", gameSession.getDefence());
        model.addAttribute("quickness", gameSession.getQuickness());
        model.addAttribute("hungriness", gameSession.getHungriness());
 
        model.addAttribute("winCount", gameSession.getWinCount());
        return "games/battle";
    }

    @PostMapping("/play")
    public String play(@RequestParam int actionType, @RequestParam int actionRate, Model model) {

        if (!judge(actionType, actionRate)) {
            
        }

        gameSession.subtractHungriness();
        gameSession.addWinCount();
        
        // DEBUG
        if (gameSession.getWinCount() > 10) {
            model.addAttribute("winCount", gameSession.getWinCount());
            model.addAttribute("hp", gameSession.getHp());
            model.addAttribute("attack", gameSession.getAttack());
            model.addAttribute("magic_attack", gameSession.getMagicAttack());
            model.addAttribute("defence", gameSession.getDefence());
            model.addAttribute("quickness", gameSession.getQuickness());
            model.addAttribute("hungriness", gameSession.getHungriness());
            return "games/result";
        }
 
        return "redirect:bonus-select";
    }

    @GetMapping("/bonus-select")
    public String showSelect() {
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
//        if (player.equals(cpu)) return "あいこ";
//        return switch (player) {
//            case "グー" -> cpu.equals("チョキ") ? "勝ち" : "負け";
//            case "チョキ" -> cpu.equals("パー") ? "勝ち" : "負け";
//            case "パー" -> cpu.equals("グー") ? "勝ち" : "負け";
//            default -> "負け";
//        };
        return true;
    }
}