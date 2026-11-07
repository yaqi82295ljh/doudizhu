package com.ddz.game.move;

import com.ddz.game.card.Card;
import com.ddz.game.pattern.CardPattern;


import java.util.*;

public record Move(List<Card> cards, CardPattern pattern) {


}
