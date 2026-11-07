package com.ddz.game.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.ddz.game.card.Card.*;

public class Deck {



    /*创建一副卡牌*/
    public static List<Card> newDeck(){

        List<Card> deck = new ArrayList<>();
        for(int s = 0; s < 4; s++){
           for(int r = 3; r <= 15; r++){
               deck.add(new Card(r, s));
           }
        }
        Card smallJoker = new Card(JOKER_SMALL,SUIT_JOKER);
        deck.add(smallJoker);
        Card bigJoker = new Card(JOKER_BIG,SUIT_JOKER);
        deck.add(bigJoker);

        return deck;
    }


    /*洗牌*/
    public static List<Card> shuffled(Random rand){
        List<Card> randDeck = newDeck();
        Collections.shuffle(randDeck,rand);
        return randDeck;
    }

    /*发牌*/
    public static List<List<Card>> deal(Random rand) {

        List<Card> allCard = shuffled(rand);

        //手牌
        List<Card> handCard1 = new ArrayList<>(allCard.subList(0,17));
        List<Card> handCard2 = new ArrayList<>(allCard.subList(17,34));
        List<Card> handCard3 = new ArrayList<>(allCard.subList(34,51));
        //底牌
        List<Card> bottomCard = new ArrayList<>(allCard.subList(51,54));

        //包含三份手牌和底牌的列表,最后一个是底牌
        List<List<Card>> cardsList = new ArrayList<>();
        cardsList.add(handCard1);
        cardsList.add(handCard2);
        cardsList.add(handCard3);
        cardsList.add(bottomCard);

        return cardsList;
    }

}
