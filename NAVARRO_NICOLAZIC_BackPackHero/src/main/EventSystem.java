package main;

import java.util.List;
import java.util.Random;

import event.DungeonEvent;
import event.EventChoice;
import hero.Hero;
import item.ItemFactory;

/**
 * Handles the logic for unpredictable events in the dungeon.
 * This system selects from a variety of random encounters and defines their
 * outcomes.
 */
public class EventSystem {
    private static final Random random = new Random();

    /**
     * Randomly selects and initializes one of the various possible dungeon events.
     * 
     * @param hero The hero participating in the event.
     * @return A DungeonEvent instance representing the selected encounter.
     */
    public static DungeonEvent getRandomEvent(Hero hero) {
        int roll = random.nextInt(16);
        return switch (roll) {
            case 0 -> createHealerEvent(hero);
            case 1 -> createMatthewEvent(hero);
            case 2 -> createMagicFontEvent(hero);
            case 3 -> createForgeEvent(hero);
            case 4 -> createBrandyEvent(hero);
            case 5 -> createAnnaEvent(hero);
            case 6 -> createFoxEvent(hero);
            case 7 -> createAlchemistEvent(hero);
            case 8 -> createSquirrelEvent(hero);
            case 9 -> createApprenticeEvent(hero);
            case 10 -> createScholarEvent(hero);
            case 11 -> createEyesEvent(hero);
            case 12 -> createArcherEvent(hero);
            case 13 -> createMasterReturnsEvent(hero);
            case 14 -> createChefEvent(hero);
            default -> createShadyTraderEvent(hero);
        };
    }

    /**
     * A basic implementation of the DungeonEvent interface.
     */
    private static class SimpleEvent implements DungeonEvent {
        String title;
        String body;
        List<EventChoice> choices;

        /**
         * Constructs a SimpleEvent.
         * 
         * @param t The title of the event.
         * @param b The body text describing the event.
         * @param c The list of possible choices for the player.
         */
        public SimpleEvent(String t, String b, List<EventChoice> c) {
            title = t;
            body = b;
            choices = c;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getBody() {
            return body;
        }

        @Override
        public List<EventChoice> getChoices() {
            return choices;
        }
    }

    /**
     * Creates an encounter with a healer.
     */
    private static DungeonEvent createHealerEvent(Hero h) {
        return new SimpleEvent("Healer", getHealerText(), getHealerChoices(h));
    }

    /**
     * Returns the flavor text for the healer event.
     */
    private static String getHealerText() {
        return "\"Come hero, let me heal you!\" A healer stands before you.\n" +
                "She removes a single quill and pokes it into your bag. \"That should do it.\"";
    }

    /**
     * Returns the list of choices for the healer event.
     */
    private static List<EventChoice> getHealerChoices(Hero h) {
        return List.of(
                new EventChoice("Remove Curses (3g)", v -> {
                    if (h.getGold() >= 3) {
                        h.addGold(-3);
                        h.getBackPack().removeAllCurses();
                        v.displayMessage("Curses removed!");
                        v.finishEvent();
                    } else
                        v.displayMessage("Not enough gold!");
                }),
                new EventChoice("Heal 25 HP (5g)", v -> {
                    if (h.getGold() >= 5) {
                        h.addGold(-5);
                        h.heal(25);
                        v.displayMessage("Healed 25 HP!");
                        v.finishEvent();
                    } else
                        v.displayMessage("Not enough gold!");
                }),
                new EventChoice("Gain 5 Max HP (10g)", v -> {
                    if (h.getGold() >= 10) {
                        h.addGold(-10);
                        h.addMaxHp(5);
                        v.displayMessage("Max HP increased!");
                        v.finishEvent();
                    } else
                        v.displayMessage("Not enough gold!");
                }),
                new EventChoice("Nothing", v -> v.finishEvent()));
    }

    /**
     * Creates a benevolent encounter with a wise rabbit named Matthew.
     */
    private static DungeonEvent createMatthewEvent(Hero hero) {
        return new SimpleEvent("Matthew's Blessing",
                "A wise rabbit meditates. \"Fear not. My name is Matthew.\"\n" +
                        "\"With proper organization, any challenge may be overcome.\"",
                List.of(
                        new EventChoice("Start with 2 uncommon items", v -> {
                            hero.getBackPack().add(ItemFactory.createUncommonItem());
                            hero.getBackPack().add(ItemFactory.createUncommonItem());
                            v.displayMessage("Matthew grants you items.");
                            v.finishEvent();
                        }),
                        new EventChoice("Gain 10 Max HP", v -> {
                            hero.addMaxHp(10);
                            v.displayMessage("Max HP increased by 10!");
                            v.finishEvent();
                        })));
    }

    /**
     * Creates an encounter with a magic font that offers varied benefits or risk of
     * combat.
     */
    private static DungeonEvent createMagicFontEvent(Hero hero) {
        return new SimpleEvent("Magic Font",
                "A magical font nestled in mushrooms. Frogs croak in the distance.\n" +
                        "Do you dare drink?",
                List.of(
                        new EventChoice("Say a prayer (+3 Max HP)", v -> {
                            hero.addMaxHp(3);
                            v.displayMessage("You feel warmth.");
                            v.finishEvent();
                        }),
                        new EventChoice("Take a bath (+15 HP)", v -> {
                            hero.heal(15);
                            v.displayMessage("Your wounds are healed.");
                            v.finishEvent();
                        }),
                        new EventChoice("Kick it (Start Battle)", v -> {
                            v.displayMessage("The frogs attack!");
                            v.startCombatFromEvent(); // Triggers combat
                        })));
    }

    /**
     * Creates an encounter with a forge (currently non-interactive).
     */
    private static DungeonEvent createForgeEvent(Hero hero) {
        return new SimpleEvent("The Forge", "A badger stands by her anvil. \"Trade me?\"",
                List.of(
                        new EventChoice("Buy Sword (15g)", v -> {
                            if (hero.getGold() >= 15) {
                                hero.addGold(-15);
                                hero.getBackPack().add(ItemFactory.createStandardSword());
                                v.displayMessage("You bought a sword.");
                                v.finishEvent();
                            } else {
                                v.displayMessage("Not enough gold!");
                            }
                        }),
                        new EventChoice("Buy Shield (15g)", v -> {
                            if (hero.getGold() >= 15) {
                                hero.addGold(-15);
                                hero.getBackPack().add(ItemFactory.createStandardShield());
                                v.displayMessage("You bought a shield.");
                                v.finishEvent();
                            } else {
                                v.displayMessage("Not enough gold!");
                            }
                        }),
                        new EventChoice("Buy Key (10g)", v -> {
                            if (hero.getGold() >= 10) {
                                hero.addGold(-10);
                                hero.getBackPack().add(ItemFactory.createSimpleKey());
                                v.displayMessage("You bought a key.");
                                v.finishEvent();
                            } else {
                                v.displayMessage("Not enough gold!");
                            }
                        }),
                        new EventChoice("Leave", v -> v.finishEvent())));
    }

    /**
     * Creates an encounter with Brandy the Otter, who sells fish.
     */
    private static DungeonEvent createBrandyEvent(Hero hero) {
        return new SimpleEvent("Brandy the Otter", "\"I've been fishing. Care to buy one?\"",
                List.of(
                        new EventChoice("Buy Fish (6g) to heal 10 HP", v -> {
                            if (hero.getGold() >= 6) {
                                hero.addGold(-6);
                                hero.heal(10);
                                v.displayMessage("Yum! (+10 HP)");
                                v.finishEvent();
                            } else
                                v.displayMessage("Too poor!");
                        }),
                        new EventChoice("No thanks", v -> v.finishEvent())));
    }

    /**
     * Creates an encounter with a statue that allows for donations or theft.
     */
    private static DungeonEvent createAnnaEvent(Hero hero) {
        return new SimpleEvent("Statue of Anna", "Inscribed: 'Anna bestowed gifts'.",
                List.of(
                        new EventChoice("Leave a coin (-1g, +5 Max HP)", v -> {
                            if (hero.getGold() >= 1) {
                                hero.addGold(-1);
                                hero.addMaxHp(5);
                                v.finishEvent();
                            }
                        }),
                        new EventChoice("Steal coins (+10g)", v -> {
                            hero.addGold(10);
                            v.displayMessage("You feel guilty.");
                            v.finishEvent();
                        })));
    }

    /**
     * Creates an encounter with a red fox who tests the player's intentions.
     */
    private static DungeonEvent createFoxEvent(Hero hero) {
        return new SimpleEvent("A Red Fox", "\"Natural prey,\" he says. \"Friend or foe?\"",
                List.of(
                        new EventChoice("Friend!", v -> {
                            v.displayMessage("He lets you pass.");
                            v.finishEvent();
                        }),
                        new EventChoice("Foe!", v -> {
                            v.displayMessage("He respects your courage and gives you his blade.");
                            hero.getBackPack().add(ItemFactory.createStandardSword());
                            v.finishEvent();
                        })));
    }

    /**
     * Creates an encounter with an alchemist trading health for items.
     */
    private static DungeonEvent createAlchemistEvent(Hero hero) {
        return new SimpleEvent("The Alchemist", "\"May I interest you in some potions? In return... just a taste.\"",
                List.of(
                        new EventChoice("Get potion (-5 HP, Get Item)", v -> {
                            hero.receiveDirectDamage(5);
                            hero.getBackPack().add(ItemFactory.createUncommonItem());
                            v.finishEvent();
                        }),
                        new EventChoice("No thanks", v -> v.finishEvent())));
    }

    /**
     * Creates an encounter with a squirrel patrol offering equipment.
     */
    private static DungeonEvent createSquirrelEvent(Hero hero) {
        return new SimpleEvent("Squirrel Patrol", "\"I don't need my old equipment. Interest you?\"",
                List.of(
                        new EventChoice("Take Shield", v -> {
                            hero.getBackPack().add(ItemFactory.createStandardShield());
                            v.finishEvent();
                        }),
                        new EventChoice("Take Weapon", v -> {
                            hero.getBackPack().add(ItemFactory.createStandardSword());
                            v.finishEvent();
                        }),
                        new EventChoice("Something to eat", v -> {
                            hero.heal(15);
                            v.finishEvent();
                        })));
    }

    /**
     * Creates an encounter with a magic apprentice giving away a wand.
     */
    private static DungeonEvent createApprenticeEvent(Hero hero) {
        return new SimpleEvent("Apprentice of Magic", "\"I'm on my second wand now. Want my first?\"",
                List.of(new EventChoice("Take Wand", v -> {
                    hero.getBackPack().add(ItemFactory.createMagicWand());
                    v.finishEvent();
                }), new EventChoice("Bye", v -> v.finishEvent())));
    }

    /**
     * Creates an encounter with a scholar selling mysterious items.
     */
    private static DungeonEvent createScholarEvent(Hero hero) {
        return new SimpleEvent("Scholar of Magic", "\"Can I interest you in a magic book?\"",
                List.of(new EventChoice("Buy Book (8g)", v -> {
                    if (hero.getGold() >= 8) {
                        hero.addGold(-8);
                        hero.getBackPack().add(ItemFactory.createUncommonItem());
                        v.finishEvent();
                    }
                }), new EventChoice("Bye", v -> v.finishEvent())));
    }

    /**
     * Creates a creepy encounter with mysterious eyes in the abyss.
     */
    private static DungeonEvent createEyesEvent(Hero hero) {
        return new SimpleEvent("Eyes", "Green eyes peer from the abyss. 01100100 0110001...",
                List.of(
                        new EventChoice("Rare Item", v -> {
                            hero.getBackPack().add(ItemFactory.createUncommonItem());
                            v.displayMessage("You got an item... and a chill spine.");
                            v.finishEvent();
                        }),
                        new EventChoice("No thanks", v -> v.finishEvent())));
    }

    /**
     * Creates an encounter with a master archer offering equipment.
     */
    private static DungeonEvent createArcherEvent(Hero hero) {
        return new SimpleEvent("Master Archer", "\"Archery is an art.\"",
                List.of(new EventChoice("Take Equipment", v -> {
                    hero.getBackPack().add(ItemFactory.createStandardSword());
                    v.displayMessage("He gives you items.");
                    v.finishEvent();
                }), new EventChoice("No thanks", v -> v.finishEvent())));
    }

    /**
     * Alias for createArcherEvent.
     */
    private static DungeonEvent createMasterReturnsEvent(Hero hero) {
        return createArcherEvent(hero);
    }

    /**
     * Creates an encounter with a chef offering a healing meal.
     */
    private static DungeonEvent createChefEvent(Hero hero) {
        return new SimpleEvent("Chef's Platter", "\"Is that grilled onion you smell?\"",
                List.of(
                        new EventChoice("Buy Food (6g)", v -> {
                            if (hero.getGold() >= 6) {
                                hero.addGold(-6);
                                hero.heal(20);
                                v.finishEvent();
                            }
                        }),
                        new EventChoice("No thanks", v -> v.finishEvent())));
    }

    /**
     * Creates an encounter with a shady trader offering a quick swap.
     */
    private static DungeonEvent createShadyTraderEvent(Hero hero) {
        return new SimpleEvent("Shady Trader", "\"Care to do a trade?\"",
                List.of(new EventChoice("Trade", v -> {
                    hero.getBackPack().add(ItemFactory.createUncommonItem());
                    v.finishEvent();
                }), new EventChoice("No", v -> v.finishEvent())));
    }

}
