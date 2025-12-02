package src.Server;

public enum MessageEnum {
    // Player-specific
	NEWUSER,
    LOGIN,
    LOGOUT,
    VIEWPROFILE,
    UPDATEBALANCE,
    UPDATEUSERNAME,
    UPDATEPASSWORD,
    
    
    // Game-specific
    JOIN_TABLE,
    PLACE_BET,
    START_ROUND,
    HIT,
    STAND,
    DOUBLE,
    SPLIT,
    GAME_STATE
}
