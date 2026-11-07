package com.ddz.game.card;

public record Card( int rank, int suit ) {

    public static final int JOKER_SMALL = 16;
    public static final int JOKER_BIG = 17;
    public static final int SUIT_JOKER = -1;

    private final static String[] RANK_TEXT = {"","","","3","4","5","6","7","8","9","10","J","Q","K","A","2","小王","大王"};
    private final static String[] SUIT_LETTER = {"S","H","C","D"};

    public String id(){
        if (rank == JOKER_SMALL && suit == SUIT_JOKER){
            return "JOKER_S";
        }
        if (rank == JOKER_BIG && suit == SUIT_JOKER){
            return "JOKER_B";
        }
        return SUIT_LETTER[suit] + RANK_TEXT[rank];
    }


    //花色字母转编号
    public static int suitOf (char letter){
        for (int i = 0; i < SUIT_LETTER.length; i++){
            if (SUIT_LETTER[i].charAt(0) == letter){
                return i;
            }
        }
        throw new IllegalArgumentException("非法花色: " + letter);
    }


    //点数文本转成rank
    public static int rankOf (String text){
        for (int r = 3; r <=15; r++){
            if (RANK_TEXT[r].equals(text)){
                return r;
            }
        }
        throw new IllegalArgumentException("非法点数: " + text);
    }


    //用id生成一张卡牌对象
    public static Card of(String id){
        if("JOKER_S".equals(id)){
            return new Card(JOKER_SMALL, SUIT_JOKER);
        }
        if("JOKER_B".equals(id)){
            return new Card(JOKER_BIG, SUIT_JOKER);
        }
        if(id == null ||id.length() < 2){
            throw new IllegalArgumentException("非法牌: " + id);
        }

        int suit = suitOf(id.charAt(0));
        int rank = rankOf(id.substring(1));
        return new Card(rank, suit);
    }

}

