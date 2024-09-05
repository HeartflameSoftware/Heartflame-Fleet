package dev.heartflame.fleet.model.s2c;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S2CActionObject {


    private String type; // internal use
    private String action; // The action (chat, disconnect, etc).
    private String bot; // The bot this action applies to ('ALL', 'RANDOM', or the username of the bot).
    private String payload; // Used for things like chat and commands that require a payload.

}
