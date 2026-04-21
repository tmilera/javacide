package com.tm.javacide.resources;
 
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.tm.javacide.javacideMain;
 
public class Panel {
 
    private Card targetCard;
    private float x, y, width, height; // remnant of previous idea also too lazy to rewrite
    private Rectangle bounds;
    private Button[] subButtons;
    private int customDrawAmount = 0; 
    private int fightBareDamage = 0;
 
    public Panel(Card targetCard, float x, float y, float width, float height) {
        this.targetCard = targetCard;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.bounds = new Rectangle(x, y, width, height);
 
        String[] buttonTexts;
        boolean[] clickables;
        int value = targetCard.getValue();
        
        boolean isEnemyCard = (targetCard.getParentDeck() != null && targetCard.getParentDeck().getDeckType() == Deck.DeckType.ENEMYDECK) ||
                              (targetCard.containedBy != null && targetCard.containedBy.tableType == Table.TableType.ENEMYTABLE) ||
                              (targetCard.containedBy != null && targetCard.containedBy == javacideMain.instance.playerClubsTable);
 
        boolean isBossCard = (targetCard.containedBy != null && targetCard.containedBy.tableType == Table.TableType.ENEMYTABLE) || 
                             (targetCard.getParentDeck() != null && targetCard.getParentDeck().getDeckType() == Deck.DeckType.ENEMYDECK);
 
        boolean isDrawingPhase = !javacideMain.deckLocked; 
        boolean inPreRound = javacideMain.playerPreRound; 
        boolean isPreRoundOver = javacideMain.deckLocked && !javacideMain.playerPreRound;
 
        if (isEnemyCard) {
            String typeName = "Enemy Card";
            if (isBossCard) {
                typeName = (value == 13) ? "King" : (value == 12) ? "Queen" : (value == 11) ? "Jack" : "Enemy Card";
            }
            
            fightBareDamage = javacideMain.instance.getEnemyDamage(targetCard);
 
            String enemyText = typeName + "\nDamage: " + fightBareDamage;
 
            String bareText;
            boolean bareClickable;
            if (isDrawingPhase) {
                bareText      = "[RED]DRAW CARDS FIRST[]";
                bareClickable = false;
            } else if (inPreRound) {
                bareText      = "[RED]IN PRE-ROUND[]";
                bareClickable = false;
            } else {
                bareText      = "Fight Bare";
                bareClickable = true;
            }
 
            if (isBossCard) {
                // Clubs remaining guard only applies to true boss cards in enemyTable.
                boolean clubsRemaining = false;
                for (Card c : javacideMain.instance.playerDeck.getCards()) {
                    if (c.containedBy == javacideMain.instance.playerClubsTable) {
                        clubsRemaining = true;
                        break;
                    }
                }
                if (clubsRemaining) {
                    bareText      = "[RED]CLUBS REMAINING[]";
                    bareClickable = false;
                }
                int suitBonus = 0;
                switch (targetCard.getSuit()) {
                    case DIAMONDS: suitBonus = 10; break;
                    case HEARTS:   suitBonus = 5;  break;
                    case CLUBS:    suitBonus = 2;  break;
                    case SPADES:   suitBonus = 0;  break;
                    default: break;
                }
                String bonusText = "[BLUE](+" + suitBonus + " Bonus)[]";
 
                buttonTexts = new String[] { enemyText, bonusText, bareText };
                clickables  = new boolean[] { false, false, bareClickable };
            } else {
                buttonTexts = new String[] { enemyText, bareText };
                clickables  = new boolean[] { false, bareClickable };
            }
            
        } else if (value == 1 && targetCard.getSuit() != Card.CardSuit.CLUBS) {
            if (isDrawingPhase) {
                buttonTexts = new String[] { "[RED]DRAW CARDS FIRST[]" };
                clickables = new boolean[] { false };
            } else if (isPreRoundOver) {
                buttonTexts = new String[] { "[RED]PRE-ROUND OVER[]" };
                clickables = new boolean[] { false };
            } else {
                buttonTexts = new String[] { "Increase deck size by 1" };
                clickables = new boolean[] { inPreRound && javacideMain.tableMaxCards < 9 };
            }
 
        } else {
            switch(targetCard.getSuit()) {
                case SPADES:
                    buttonTexts = new String[] { isDrawingPhase ? "[RED]DRAW CARDS FIRST[]" : (inPreRound ? "[RED]IN PRE-ROUND[]" : "Attack for: " + value) };
                    clickables = new boolean[] { !isDrawingPhase && !inPreRound }; 
                    break;
                case DIAMONDS:
                    // FIX: Calculates available space correctly by ignoring clubs
                    int availableSpace = javacideMain.tableMaxCards - (javacideMain.instance.getCardsInHand() - 1);
                    customDrawAmount = Math.max(0, Math.min(value, availableSpace));
                    
                    String drawText;
                    boolean drawAllowed;
 
                    if (isDrawingPhase) {
                        drawText = "[RED]DRAW CARDS FIRST[]";
                        drawAllowed = false;
                    } else if (isPreRoundOver) {
                        drawText = "[RED]PRE-ROUND OVER[]";
                        drawAllowed = false;
                    } else if (availableSpace <= 0) {
                        drawText = "[RED]HAND FULL[]";
                        drawAllowed = false;
                    } else {
                        drawText = "Draw for: " + customDrawAmount;
                        drawAllowed = inPreRound && !javacideMain.hasDrawnThisRound && customDrawAmount > 0;
                    }
                    
                    String attackText = isDrawingPhase ? "[RED]DRAW CARDS FIRST[]" : (inPreRound ? "[RED]IN PRE-ROUND[]" : "Attack for: " + value);
                    buttonTexts = new String[] { drawText, attackText };
                    clickables = new boolean[] { drawAllowed, !isDrawingPhase && !inPreRound };
                    break;
                case HEARTS:
                    if (isDrawingPhase) {
                        buttonTexts = new String[] { "[RED]DRAW CARDS FIRST[]" };
                        clickables = new boolean[] { false };
                    } else if (inPreRound) {
                        buttonTexts = new String[] { "[RED]IN PRE-ROUND[]" };
                        clickables = new boolean[] { false };
                    } else if (javacideMain.hasHealedThisTurn) {
                        buttonTexts = new String[] { "[RED]PER TURN ONLY[]" };
                        clickables = new boolean[] { false };
                    } else if (javacideMain.playerHealth >= 50) {
                        buttonTexts = new String[] { "[RED]AT MAX HP[]" };
                        clickables = new boolean[] { false };
                    } else {
                        buttonTexts = new String[] { "Heal for: " + value };
                        clickables = new boolean[] { true };
                    }
                    break;
                default:
                    buttonTexts = new String[0];
                    clickables = new boolean[0];
                    break;
            }
        }
 
        int numButtons = buttonTexts.length;
        this.subButtons = new Button[numButtons];
 
        float padding = 10f;
        float btnWidth = width - (padding * 2);
        
        int maxSlots = Math.max(2, numButtons);
        
        float slotHeight = (height - (padding * (maxSlots + 1))) / maxSlots;
        float btnHeight = slotHeight - 6f; 
 
        for (int i = 0; i < numButtons; i++) {
            float btnX = x + padding;
            float slotY = y + height - padding - (slotHeight * (i + 1)) - (padding * i);
            float btnY = slotY + 3f; 
            
            subButtons[i] = new Button(Button.ButtonType.PANEL, btnX, btnY, btnWidth, btnHeight, buttonTexts[i]);
            subButtons[i].setClickable(clickables[i]);
        }
    }
 
    public Card getTargetCard() {
        return targetCard;
    }
 
    public boolean isHovered() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        javacideMain.viewport.unproject(mouse);
        return bounds.contains(mouse.x, mouse.y);
    }
 
    public void render(SpriteBatch batch) {
        for (int i = 0; i < subButtons.length; i++) {
            Button b = subButtons[i];
            b.render(batch);
 
            if (b.isClicked()) {
                handleAction(i);
            }
        }
    }
 
    private void handleAction(int buttonIndex) {
        if (targetCard.getValue() == 1 && targetCard.getSuit() != Card.CardSuit.CLUBS) {
            if (buttonIndex == 0) {
                javacideMain.tableMaxCards = Math.min(9, javacideMain.tableMaxCards + 1);
                javacideMain.playerPreRound = false; 
                targetCard.getParentDeck().removeCard(targetCard);
                closePanel();
            }
            return;
        }
 
        boolean isEnemyCard = (targetCard.getParentDeck() != null && targetCard.getParentDeck().getDeckType() == Deck.DeckType.ENEMYDECK) ||
                              (targetCard.containedBy != null && targetCard.containedBy.tableType == Table.TableType.ENEMYTABLE) ||
                              (targetCard.containedBy != null && targetCard.containedBy == javacideMain.instance.playerClubsTable);
 
        if (isEnemyCard) {
            boolean isBossCard = (targetCard.containedBy != null && targetCard.containedBy.tableType == Table.TableType.ENEMYTABLE) || 
                                 (targetCard.getParentDeck() != null && targetCard.getParentDeck().getDeckType() == Deck.DeckType.ENEMYDECK);
            
            int bareIndex = isBossCard ? 2 : 1;
            
            if (buttonIndex == bareIndex) {
                javacideMain.playerHealth -= fightBareDamage;
                System.out.println("Fought Bare! Player Health is now: " + javacideMain.playerHealth);
                
                javacideMain.hasHealedThisTurn = false;
                
                targetCard.getParentDeck().removeCard(targetCard);
                
                if (isBossCard) {
                    javacideMain.instance.nextRound();
                }
                closePanel();
            }
            return;
        }
 
        switch(targetCard.getSuit()) {
            case SPADES:
                if (buttonIndex == 0) {
                    javacideMain.attackingCard = targetCard;
                    javacideMain.justStartedAttack = true; 
                    closePanel();
                }
                break;
            case DIAMONDS:
                if (buttonIndex == 0) {
                    javacideMain.hasDrawnThisRound = true;
                    javacideMain.playerPreRound = false; 
                    targetCard.getParentDeck().removeCard(targetCard);
                    javacideMain.instance.drawSpecificAmount(customDrawAmount);
                    closePanel();
                } else if (buttonIndex == 1) {
                    javacideMain.attackingCard = targetCard;
                    javacideMain.justStartedAttack = true; 
                    closePanel();
                }
                break;
            case HEARTS:
                if (buttonIndex == 0) {
                    javacideMain.playerHealth = Math.min(50, javacideMain.playerHealth + targetCard.getValue());
                    System.out.println("Healed! Player Health is now: " + javacideMain.playerHealth);
                    
                    javacideMain.hasHealedThisTurn = true;
                    
                    targetCard.getParentDeck().removeCard(targetCard);
                    closePanel();
                }
                break;
            case CLUBS:
                break;
            default:
                break;
        }
    }
    
    private void closePanel() {
        javacideMain.activePanel.dispose();
        javacideMain.activePanel = null;
    }
 
    public void dispose() {
        for (Button b : subButtons) {
            b.dispose();
        }
    }
}