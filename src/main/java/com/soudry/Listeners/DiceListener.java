package com.soudry.Listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
// import javax.management.AttributeList;
public class DiceListener extends ListenerAdapter {

    private ArrayList<String> resultList = new ArrayList<>();

    private int whichCase(String i) {
        if (i.contains("d")) {
            return 0;
        } else if (i.equals("+")) {
            return 1;
        }
        else if (i.equals("-")) {
            return 2;
        } else if (isNumber(i)) {
            return 3;
        } else {
            return 4;
        }
    }

    private int calculateAmount(int amount, int addend, String operator) {
        if (operator == "+" || operator == "") {
            amount += addend;
        } else {
            amount -= addend;
        }
        return amount;
    }

    private static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void addToLog(int[] roll, int sum) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < roll.length; i++) { 
            if (i > 0) {
                string.append(", ");
            }
            string.append(roll[i]);
        }
        String s = String.format("( %s ) **%d**", string.toString(), sum);
        this.resultList.add(s);
    }

    private int parseDice(String d20) {
        String[] array = d20.split("d");
        int sum = 0;
        int length = Integer.valueOf(array[0]);
        int[] roll = new int[length];
        int dice = Integer.valueOf(array[1]);
        for (int i = 0; i < length; i++) {
            roll[i] = (int) Math.floor((Math.random() * dice) + 1);
            sum += roll[i];
        }
        addToLog(roll, sum);
        return sum;
    }
  
    private int transformString(String eventMessage) {
        String[] items = eventMessage.split(" ");
        int amount = 0;
        String operator = "";
        int addend = 0;
        for (String i : items) {
           this.resultList.add(i);
           int caseValue = whichCase(i);
            switch (caseValue) {
                case 0:
                    addend = parseDice(i);
                    amount = calculateAmount(amount, addend, operator);
                    operator = "";
                    break;
                case 1:
                    operator = "+";
                    break; 
                case 2:
                    operator = "-";
                    break; 
                case 3:
                    addend = Integer.valueOf(i);
                    amount = calculateAmount(amount, addend, operator);
                    operator = "";
                    break; 
                default:
                    continue;
            }
        }
        return amount;
    }

    private String resultOutput() {
        StringBuilder string = new StringBuilder();
        String currentValue = "";
        for (int i = 0; i < this.resultList.size(); i++) {
            currentValue = this.resultList.get(i);
            string.append(currentValue);
            string.append(" ");
        }
        this.resultList.clear();
        return string.toString();
    }

    private String[] getAmountAndResult(String message) {
        String[] amountResult = new String[2];
        amountResult[0] = String.valueOf(transformString(message.substring(6)));
        amountResult[1] = String.format("%s \n", resultOutput());
        return amountResult;
    }
    
    private void sendEvent(@Nonnull MessageReceivedEvent event, String result, int amount) {
        MessageCreateData data = new MessageCreateBuilder()
        .addContent(event.getAuthor().getAsMention() + "\n")
        .addContent(String.format("**Result**: %s", result))
        // .addContent(String.format("**Result** : %s ", result))
        .addContent(String.format("**Total**:  %d", amount))
        .build();
       event.getChannel().sendMessage(data).queue();

    }
    
    private void standardRoll(@Nonnull MessageReceivedEvent event, String message) {
             String [] amountResult = getAmountAndResult(message);
             int amount = Integer.valueOf(amountResult[0]);
             String result = amountResult[1];
             sendEvent(event, result, amount);
            //  MessageCreateData data = new MessageCreateBuilder()
            //  .addContent(event.getAuthor().getAsMention() + "\n")
            //  .addContent(result)
            //  .addContent(String.format("**Total**:  %d", amount))
            //  .build();
            // event.getChannel().sendMessage(data).queue();
    }

    private void advantageDisadvantageRoll(@Nonnull MessageReceivedEvent event, String message, Boolean advantage) {
        String[] firstResult = getAmountAndResult(message);
        int first = Integer.valueOf(firstResult[0]);
        String[] secondResult = getAmountAndResult(message);
        int second = Integer.valueOf(secondResult[0]);
        Map<Integer, String> map = new HashMap<>();
        map.put(first, firstResult[1]);
        map.put(second, secondResult[1]);
        int higher = Math.max(first, second);
        int lower = Math.min(first, second);
        
        String result = "";
        String loss = "";
        if (advantage) {
           result = map.get(higher);
           loss = map.get(lower);
           String rollout = String.format("%s ~~%s~~", result, loss);
           sendEvent(event, rollout, higher);

        }
        else {
            result = map.get(lower);
            loss = map.get(higher);
            
            String rollout = String.format("%s ~~%s~~", result, loss);
            sendEvent(event, rollout, lower);

        }
       
    }

    @Override
    public void onMessageReceived( @Nonnull MessageReceivedEvent event) {
    
        if (!event.getAuthor().isBot()) {
            String message = event.getMessage().getContentRaw();

            if (message.contains("roll")) {
                standardRoll(event, message);
            } else if ( message.contains("radv")) {
                advantageDisadvantageRoll(event, message, true);

            } else if (message.contains("rdis")) {
                advantageDisadvantageRoll(event, message, false);
            }
            
        }
    }
}     