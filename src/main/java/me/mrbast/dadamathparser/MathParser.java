package me.mrbast.dadamathparser;


public class MathParser {

    public static Expression parse(final String str) {
        return new Object() {
            int pos = -1, ch;

            // Avanza al carattere successivo
            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            // Consuma il carattere se corrisponde a quello atteso (ignorando gli spazi)
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            // Metodo principale di avvio
            Expression parse() {
                nextChar();
                Expression x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Carattere inatteso: " + (char)ch);
                return x;
            }

            // Analizza addizioni e sottrazioni
            Expression parseExpression() {
                Expression x = parseTerm();
                for (;;) {
                    if      (eat('+')) { Expression a = x, b = parseTerm(); x = vars -> a.evaluate(vars) + b.evaluate(vars); }
                    else if (eat('-')) { Expression a = x, b = parseTerm(); x = vars -> a.evaluate(vars) - b.evaluate(vars); }
                    else return x;
                }
            }

            // Analizza moltiplicazioni e divisioni
            Expression parseTerm() {
                Expression x = parseFactor();
                for (;;) {
                    if      (eat('*')) { Expression a = x, b = parseFactor(); x = vars -> a.evaluate(vars) * b.evaluate(vars); }
                    else if (eat('/')) { Expression a = x, b = parseFactor(); x = vars -> a.evaluate(vars) / b.evaluate(vars); }
                    else return x;
                }
            }

            // Analizza numeri, variabili, parentesi e segni unari
            Expression parseFactor() {
                if (eat('+')) return parseFactor(); // Più unario (+5)
                if (eat('-')) { Expression a = parseFactor(); return vars -> -a.evaluate(vars); } // Meno unario (-5)

                Expression x;
                int startPos = this.pos;
                
                if (eat('(')) { // Parentesi
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // Numeri (es. 0.1)
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    double val = Double.parseDouble(str.substring(startPos, this.pos));
                    x = vars -> val;
                } else if (ch == '%' || Character.isLetter(ch)) { // Variabili/Placeholder (es. %money% o price)
                    // Accetta lettere, numeri, underscore e il simbolo %
                    while (ch == '%' || Character.isLetterOrDigit(ch) || ch == '_') nextChar();
                    String varName = str.substring(startPos, this.pos);
                    
                    x = vars -> {
                        Double val = vars.get(varName);
                        if (val == null) {
                            throw new IllegalArgumentException("Missing placeholder value for : " + varName);
                        }
                        return val;
                    };
                } else {
                    throw new RuntimeException("Unexpected character: " + (char)ch);
                }

                return x;
            }
        }.parse();
    }
}