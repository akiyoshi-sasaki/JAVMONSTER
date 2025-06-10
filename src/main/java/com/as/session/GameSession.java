package com.as.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import lombok.Data;

@Component
@Data
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GameSession {
    private int hp = 20;
    private int attack = 10;
    private int magicAttack = 0;
    private int defence = 10;
    private int quickness = 10;
    private int hungriness = 10;
    private int winCount = 0;    

    public void addWinCount() {
        ++this.winCount;
    }
    
    public void addBonus(int bonusType, int bonusValue) {
        switch (bonusType) {
            case 1:
                this.hp += bonusValue;
                break;
            case 2:
                this.attack += bonusValue;
                break;
            case 3:
                this.magicAttack += bonusValue;
                break;
            case 4:
                this.defence += bonusValue;
                break;
            case 5:
                this.quickness += bonusValue;
                break;
            case 6:
                this.hungriness += bonusValue;
                break;
        }
    }

    public void subtractHungriness() {
        --this.hungriness;
    }
    
    public void reset() {
        this.hp = 100;
        this.attack = 10;
        this.magicAttack = 0;
        this.defence = 10;
        this.quickness = 10;
        this.hungriness = 10;
        this.winCount = 0;
    }

}