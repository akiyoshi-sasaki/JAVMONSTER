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
    private int hp = 100;
    private int attack = 100;
    private int magicAttack = 1;
    private int defence = 100;
    private int quickness = 100;
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
        this.attack = 100;
        this.magicAttack = 1;
        this.defence = 100;
        this.quickness = 100;
        this.hungriness = 10;
        this.winCount = 0;
    }

}