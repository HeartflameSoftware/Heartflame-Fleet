package dev.heartflame.fleet.model.s2c;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class S2CActionObject {


    private String action; // The action (chat, disconnect, etc).
    private String bot; // The bot this action applies to ('ALL', 'RANDOM', or the username of the bot).

}
