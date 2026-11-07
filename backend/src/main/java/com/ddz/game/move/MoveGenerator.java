package com.ddz.game.move;

import com.ddz.game.card.Card;
import com.ddz.game.pattern.CardPattern;
import com.ddz.game.pattern.PatternComparator;
import com.ddz.game.pattern.PatternParser;
import com.ddz.game.pattern.PatternType;

import java.util.*;

public class MoveGenerator {

    public static List<Move> beatingMoves(List<Card> hand, CardPattern last) {

        if (last == null) {
            return new ArrayList<>();
        }

        Map<Integer, List<Card>> byRank = groupByRank(hand);
        List<Move> moves = new ArrayList<>();


        if (last.type() == PatternType.SINGLE) {
            addCandidates(moves, byRank, 1, last);
        } else if (last.type() == PatternType.PAIR) {
            addCandidates(moves, byRank, 2, last);
        } else if (last.type() == PatternType.TRIPLE) {
            addCandidates(moves, byRank, 3, last);
        } else if (last.type() == PatternType.STRAIGHT) {
            addSequences(moves, byRank, last.count(), 1, last);
        } else if (last.type() == PatternType.DOUBLE_STRAIGHT) {
            addSequences(moves, byRank, last.count() / 2, 2, last);
        } else if (last.type() == PatternType.TRIPLE_WITH_SINGLE) {
            addTripleWithWing(moves, byRank, 1, last);
        } else if (last.type() == PatternType.TRIPLE_WITH_PAIR) {
            addTripleWithWing(moves, byRank, 2, last);
        } else if (last.type() == PatternType.AIRPLANE) {
            addAirplane(moves, byRank, last.count() / 3, 0, last);
        } else if (last.type() == PatternType.AIRPLANE_WITH_SINGLE) {
            addAirplane(moves, byRank, last.count() / 4, 1, last);
        } else if (last.type() == PatternType.AIRPLANE_WITH_PAIR) {
            addAirplane(moves, byRank, last.count() / 5, 2, last);
        } else if (last.type() == PatternType.FOUR_WITH_TWO_SINGLES) {
            addFourWithWings(moves, byRank, 1, last);
        } else if (last.type() == PatternType.FOUR_WITH_TWO_PAIRS) {
            addFourWithWings(moves, byRank, 2, last);
        }

        //炸弹 + 王炸
        addBombs(moves, byRank, last);

        return moves;
    }

    //首出枚举
    public static List<Move> allMoves(List<Card> hand) {
        Map<Integer, List<Card>> byRank = groupByRank(hand);
        List<Move> moves = new ArrayList<>();

        // 单张 / 对子 / 三张
        addCandidates(moves, byRank, 1, null);
        addCandidates(moves, byRank, 2, null);
        addCandidates(moves, byRank, 3, null);

        // 顺子
        for (int len = 5; len <= 12; len++) {
            addSequences(moves, byRank, len, 1, null);
        }
        // 连对
        for (int pairs = 3; pairs <= 12; pairs++) {
            addSequences(moves, byRank, pairs, 2, null);
        }

        // 三带一 / 三带二
        addTripleWithWing(moves, byRank, 1, null);
        addTripleWithWing(moves, byRank, 2, null);

        // 飞机
        for (int n = 2; n <= 5; n++) {
            addAirplane(moves, byRank, n, 0, null);
        }
        for (int n = 2; n <= 4; n++) {
            addAirplane(moves, byRank, n, 1, null);
        }
        for (int n = 2; n <= 3; n++) {
            addAirplane(moves, byRank, n, 2, null);
        }

        // 四带二
        addFourWithWings(moves, byRank, 1, null);
        addFourWithWings(moves, byRank, 2, null);

        //炸弹 + 王炸
        addBombs(moves, byRank, null);

        return moves;
    }

    /** 把手牌按点数分组 */
    private static Map<Integer, List<Card>> groupByRank(List<Card> hand) {
        Map<Integer, List<Card>> byRank = new HashMap<>();
        for (Card c : hand) {
            byRank.computeIfAbsent(c.rank(), k -> new ArrayList<>()).add(c);
        }
        return byRank;
    }

    /** 炸弹与王炸 */
    private static void addBombs(List<Move> moves, Map<Integer, List<Card>> byRank, CardPattern last) {
        for (Integer rank : byRank.keySet()) {
            if (byRank.get(rank).size() == 4) {
                addIfBeat(moves, take(byRank, rank, 4), last);
            }
        }
        if (byRank.containsKey(Card.JOKER_SMALL) && byRank.containsKey(Card.JOKER_BIG)) {
            List<Card> rocket = new ArrayList<>();
            rocket.add(byRank.get(Card.JOKER_SMALL).get(0));
            rocket.add(byRank.get(Card.JOKER_BIG).get(0));
            addIfBeat(moves, rocket, last);
        }
    }

    /** 枚举"同一种点数取 size 张"的候选 */
    private static void addCandidates(List<Move> moves, Map<Integer, List<Card>> byRank,
                                      int size, CardPattern last) {
        for (Integer rank : byRank.keySet()) {
            if (byRank.get(rank).size() >= size) {
                addIfBeat(moves, take(byRank, rank, size), last);
            }
        }
    }

    /** 从某个点数里取前 n 张 */
    private static List<Card> take(Map<Integer, List<Card>> byRank, int rank, int n) {
        return new ArrayList<>(byRank.get(rank).subList(0, n));
    }

    /** 解析 → 判断能否压过 last → 加入结果 */
    private static void addIfBeat(List<Move> moves, List<Card> cards, CardPattern last) {
        PatternParser.parse(cards).ifPresent(p -> {
            if (PatternComparator.canBeat(p, last)) {
                moves.add(new Move(cards, p));
            }
        });
    }

    /** 飞机 **/
    private static void addAirplane(List<Move> moves, Map<Integer, List<Card>> byRank,
                                    int n, int wingPerKind, CardPattern last) {
        if (n < 2) {
            return;
        }
        // 主体不能含 2 和王，所以最大点数封顶 14
        for (int start = 3; start + n - 1 <= 14; start++) {
            List<Card> body = new ArrayList<>();
            Set<Integer> used = new HashSet<>();
            boolean ok = true;
            for (int r = start; r < start + n; r++) {
                List<Card> same = byRank.get(r);
                if (same == null || same.size() < 3) {
                    ok = false;
                    break;
                }
                body.addAll(take(byRank, r, 3));
                used.add(r);
            }
            if (!ok) {
                continue;
            }
            if (wingPerKind == 0) {
                addIfBeat(moves, body, last);
                continue;
            }
            List<Integer> wingRanks = new ArrayList<>(byRank.keySet());
            wingRanks.removeAll(used);
            List<List<Card>> wings = new ArrayList<>();
            collectWings(byRank, wingRanks, 0, n, wingPerKind, new ArrayList<>(), wings);
            for (List<Card> wing : wings) {
                List<Card> all = new ArrayList<>(body);
                all.addAll(wing);
                addIfBeat(moves, all, last);
            }
        }
    }

    /** 枚举四带二 **/
    private static void addFourWithWings(List<Move> moves, Map<Integer, List<Card>> byRank,
                                         int wingPerKind, CardPattern last) {
        for (Integer fourRank : byRank.keySet()) {
            if (byRank.get(fourRank).size() != 4) {
                continue;
            }
            List<Card> body = take(byRank, fourRank, 4);
            List<Integer> wingRanks = new ArrayList<>(byRank.keySet());
            wingRanks.remove(fourRank);
            List<List<Card>> wings = new ArrayList<>();
            collectWings(byRank, wingRanks, 0, 2, wingPerKind, new ArrayList<>(), wings);
            for (List<Card> wing : wings) {
                List<Card> all = new ArrayList<>(body);
                all.addAll(wing);
                addIfBeat(moves, all, last);
            }
        }
    }

    private static void collectWings(Map<Integer, List<Card>> byRank, List<Integer> ranks, int idx,
                                     int need, int perKind, List<Card> current,
                                     List<List<Card>> out) {
        if (need == 0) {
            out.add(new ArrayList<>(current));
            return;
        }
        if (idx >= ranks.size()) {
            return;
        }
        int rank = ranks.get(idx);
        int maxTake = Math.min(need, byRank.get(rank).size() / perKind);
        for (int t = 0; t <= maxTake; t++) {
            int before = current.size();
            if (t > 0) {
                current.addAll(take(byRank, rank, t * perKind));
            }
            collectWings(byRank, ranks, idx + 1, need - t, perKind, current, out);
            while (current.size() > before) {
                current.remove(current.size() - 1);
            }
        }
    }

    /** 枚举三带一 / 三带二 **/
    private static void addTripleWithWing(List<Move> moves, Map<Integer, List<Card>> byRank,
                                          int wingSize, CardPattern last) {
        for (Integer tripleRank : byRank.keySet()) {
            if (byRank.get(tripleRank).size() < 3) {
                continue;
            }
            for (Integer wingRank : byRank.keySet()) {
                if (wingRank.equals(tripleRank) || byRank.get(wingRank).size() < wingSize) {
                    continue;
                }
                List<Card> picked = new ArrayList<>(take(byRank, tripleRank, 3));
                picked.addAll(take(byRank, wingRank, wingSize));
                addIfBeat(moves, picked, last);
            }
        }
    }

    /** 枚举顺子，连队 **/
    private static void addSequences(List<Move> moves, Map<Integer, List<Card>> byRank,
                                     int kindCount, int perKind, CardPattern last) {
        if (kindCount < 2) {
            return;
        }
        // 最大点数14
        for (int start = 3; start + kindCount - 1 <= 14; start++) {
            List<Card> picked = new ArrayList<>();
            boolean ok = true;
            for (int r = start; r < start + kindCount; r++) {
                List<Card> same = byRank.get(r);
                if (same == null || same.size() < perKind) {
                    ok = false;
                    break;
                }
                picked.addAll(take(byRank, r, perKind));
            }
            if (ok) {
                addIfBeat(moves, picked, last);
            }
        }
    }

}
