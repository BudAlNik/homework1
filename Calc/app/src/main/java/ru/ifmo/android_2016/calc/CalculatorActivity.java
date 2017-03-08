package ru.ifmo.android_2016.calc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import ru.ifmo.android_2016.calc.ParseException;

/**
 * Created by alexey.nikitin on 13.09.16.
 */

public final class CalculatorActivity extends Activity {
    private double num;
    private TextView result, expression;
    private String expr;
    private String expr_on_screen;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calculator);

        num = Double.NaN;
        result = (TextView) findViewById(R.id.result);
        expression = (TextView) findViewById(R.id.expression);
        context = getApplicationContext();
        expr = "";
        expr_on_screen = "";
        expression.setText(expr_on_screen);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("expr", expr);
        outState.putString("expr_on_screen", expr_on_screen);
        Log.d("MyTag", expr_on_screen);
        if (!Double.isNaN(num)) {
            outState.putDouble("num", num);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        num = savedInstanceState.getDouble("num", Double.NaN);
        if (!Double.isNaN(num)) {
            result.setText(String.format("%.6f", num));
        } else {
            result.setText("");
        }
        expr = savedInstanceState.getString("expr");
        expr_on_screen = savedInstanceState.getString("expr_on_screen");
        if (expr_on_screen != null) {
            expression.setText(expr_on_screen);
        }
    }

    protected void btnClick(View view) {
        switch (view.getId()) {
            case R.id.clear:
                expr = "";
                expr_on_screen = "";
                result.setText("");
                break;
            case R.id.backspace:
                expr = expr.substring(0, expr.length() - 1);
                expr_on_screen = expr_on_screen.substring(0, expr_on_screen.length() - 1);
                break;
            case R.id.div:
                expr += "/";
                break;
            case R.id.dot:
                expr += ".";
                break;
            case R.id.mul:
                expr += "*";
                break;
            case R.id.sub:
                expr += "-";
                break;
            case R.id.add:
                expr += "+";
                break;
            case R.id.br_o:
                expr += "(";
                break;
            case R.id.br_c:
                expr += ")";
                break;
        }
        if (view.getId() != R.id.clear && view.getId() != R.id.backspace) {
            expr_on_screen += ((TextView) view).getText();
        }
        expression.setText(expr_on_screen);
    }

    protected void digitClick(View view) {
        expr += ((TextView) view).getText();
        expr_on_screen += ((TextView) view).getText();
        expression.setText(expr_on_screen);
    }

    private int pos;

    boolean isSign(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    int getSignDepth(char c) throws ParseException {
        if (c == '+' || c == '-') {
            return 0;
        }
        if (c == '*' || c == '/') {
            return 1;
        }
        throw new ParseException("Invalid sign");
    }

    double implementSign(double a, double b, char sign) throws ParseException {
        switch(sign) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                return a / b;
            default:
                throw new ParseException("Invalid sign");
        }
    }

    double parseBinary(int depth) throws ParseException {
        if (depth == 2) {
            return parseUnary();
        }
        double ret = parseBinary(depth + 1);
        while (true) {
            if (pos == expr.length() || !isSign(expr.charAt(pos)) || getSignDepth(expr.charAt(pos)) != depth) {
                return ret;
            }
            char sign = expr.charAt(pos);
            pos += 1;
            ret = implementSign(ret, parseBinary(depth + 1), sign);
        }
    }

    double parseUnary() throws ParseException {
        if (pos >= expr.length()) {
            throw new ParseException("Unexpected end of expression");
        }
        if (expr.charAt(pos) == '-') {
            pos += 1;
            return -parseUnary();
        }
        if (expr.charAt(pos) == '+') {
            pos += 1;
            return parseUnary();
        }
        if (expr.charAt(pos) == '(') {
            pos += 1;
            double ret = parseBinary(0);
            if (pos >= expr.length() || expr.charAt(pos) != ')') {
                throw new ParseException("Incorrect brackets sequence");
            }
            pos += 1;
            return ret;
        }
        String number = "";
        while (pos < expr.length() && (('0' <= expr.charAt(pos) && expr.charAt(pos) <= '9') || expr.charAt(pos) == '.')) {
            number += expr.charAt(pos);
            pos += 1;
        }
        try {
            return Double.parseDouble(number);
        } catch (NumberFormatException e) {
            Log.d("MyTag", "Incorrect number " + number);
            throw new ParseException("Incorrect number");
        }
    }

    double evaluate() {
        pos = 0;
        try {
            double ret = parseBinary(0);
            if (pos != expr.length()) {
                return Double.NaN;
            }
            return ret;
        } catch (ParseException e) {
            return Double.NaN;
        }
    }

    protected void calc(View view) {
        Log.d("MyTag", "Run calc()");
        num = evaluate();
        if (Double.isNaN(num)) {
            result.setText("Error");
        } else {
            result.setText(String.format("%.6f", num));
        }
    }
}
