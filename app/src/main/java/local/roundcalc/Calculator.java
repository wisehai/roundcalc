package local.roundcalc;

import java.math.*;

/** Decimal arithmetic with operator precedence; programmer mode uses exact integers. */
public final class Calculator {
    private static final MathContext DISPLAY_MC = new MathContext(16, RoundingMode.HALF_UP);

    public static String evaluate(String expression, int base, boolean integer) {
        Parser p = new Parser(expression, base, integer);
        BigDecimal result = p.sum();
        if (p.i != p.s.length()) throw new IllegalArgumentException("表达式未完成");
        if (integer) {
            return result.toBigIntegerExact().toString(base).toUpperCase(java.util.Locale.ROOT);
        }
        if (result.scale() > 16 && result.precision() > 16) {
            result = result.round(DISPLAY_MC);
        }
        result = result.stripTrailingZeros();
        if (result.compareTo(BigDecimal.ZERO) == 0) return "0";
        return result.toPlainString();
    }
    private static final class Parser {
        final String s; final int base; final boolean integer; int i;
        Parser(String s, int base, boolean integer) {
            this.s=s.replace(" ", ""); this.base=base; this.integer=integer;
            if (s.length()>2048) throw new IllegalArgumentException("输入过长");
        }
        boolean take(char c) { if(i<s.length() && s.charAt(i)==c) { i++; return true; } return false; }
        BigDecimal sum() {
            BigDecimal n=product();
            while(i<s.length()) {
                if(take('+')) n=n.add(product());
                else if(take('−') || take('-')) n=n.subtract(product());
                else break;
            } return n;
        }
        BigDecimal product() {
            BigDecimal n=number();
            while(i<s.length()) {
                if(take('×') || take('*')) n=n.multiply(number());
                else if(take('÷') || take('/')) {
                    BigDecimal d=number();
                    n=integer ? new BigDecimal(n.toBigIntegerExact().divide(d.toBigIntegerExact())) : n.divide(d, MathContext.DECIMAL128);
                } else break;
                if(n.precision()>4096) throw new IllegalArgumentException("结果过长");
            } return n;
        }
        BigDecimal number() {
            if(take('−') || take('-')) return number().negate();
            if(take('+')) return number();
            int start=i;
            while(i<s.length() && (Character.digit(s.charAt(i),base)>=0 || (!integer && s.charAt(i)=='.'))) i++;
            if(start==i) throw new IllegalArgumentException("表达式未完成");
            String token=s.substring(start,i);
            BigDecimal n=integer ? new BigDecimal(new BigInteger(token,base)) : new BigDecimal(token);
            while(take('%')) { if(integer) throw new IllegalArgumentException("进制模式仅支持整数"); n=n.movePointLeft(2); }
            return n;
        }
    }
}
