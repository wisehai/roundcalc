package local.roundcalc;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.math.*;
import java.util.*;

public class MainActivity extends Activity {
    final int orange=0xffff7417, muted=0xff888888, white=0xffeeeeee;
    boolean programmer=false, fresh=false; int base=10;
    String decimal="", radix="", history="";
    LinearLayout root, display, conversions, letters, keyboard;
    TextView expression, preview, caption; ScrollView expressionScroll; Button basicTab, radixTab;
    final Map<String,Button> keys=new HashMap<>();
    final Map<Integer,TextView> values=new HashMap<>();
    final int[] bases={16,10,8,2}; final String[] names={"HEX","DEC","OCT","BIN"};
    int dp(float v) { return (int)(getResources().getDisplayMetrics().density*v+.5f); }
    String input() { return programmer?radix:decimal; }
    void input(String s) { if(programmer) radix=s; else decimal=s; }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.BLACK); getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        android.content.SharedPreferences p=getSharedPreferences("state",0);
        decimal=p.getString("decimal",""); radix=p.getString("radix","");
        programmer=p.getBoolean("programmer",false); base=p.getInt("base",10); history=p.getString("history","");
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(16),dp(8),dp(16),dp(12)); root.setBackgroundColor(Color.BLACK);
        setContentView(root);
        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        Button h=button("◷",0xff252525,22); h.setContentDescription("计算历史"); h.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("计算历史").setMessage(history.isEmpty()?"暂无记录":history).setPositiveButton("关闭",null).setNeutralButton("清空",(d,w)->{history="";save();}).show());
        bar.addView(h,new LinearLayout.LayoutParams(dp(42),dp(42)));
        basicTab=button("基础计算",0xff000000,15); radixTab=button("进制转换",0xff000000,15);
        bar.addView(basicTab,new LinearLayout.LayoutParams(0,dp(44),1)); bar.addView(radixTab,new LinearLayout.LayoutParams(0,dp(44),1));
        basicTab.setOnClickListener(v->switchMode(false)); radixTab.setOnClickListener(v->switchMode(true)); root.addView(bar);
        display=new LinearLayout(this); display.setOrientation(LinearLayout.VERTICAL); display.setGravity(Gravity.BOTTOM); display.setPadding(dp(6),dp(4),dp(6),dp(8));
        root.addView(display,new LinearLayout.LayoutParams(-1,0,1));
        caption=text("",12,muted); caption.setGravity(Gravity.RIGHT); display.addView(caption);
        ScrollView scroll=new ScrollView(this); expressionScroll=scroll; scroll.setFillViewport(true); scroll.setVerticalScrollBarEnabled(false);
        expression=text("0",44,white); expression.setGravity(Gravity.RIGHT|Gravity.BOTTOM); expression.setSingleLine(false); expression.setTextIsSelectable(true);
        scroll.addView(expression,new android.widget.FrameLayout.LayoutParams(-1,-2)); display.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        preview=text("",18,muted); preview.setGravity(Gravity.RIGHT); display.addView(preview);
        conversions=new LinearLayout(this); conversions.setOrientation(LinearLayout.VERTICAL); display.addView(conversions);
        for(int j=0;j<4;j++) {
            final int r=bases[j]; LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(10),0,dp(10),0);
            TextView label=text(names[j],12,muted); row.addView(label,new LinearLayout.LayoutParams(dp(42),-2));
            HorizontalScrollView sv=new HorizontalScrollView(this); sv.setHorizontalScrollBarEnabled(false); sv.setFillViewport(true);
            TextView value=text("0",16,white); value.setTypeface(Typeface.MONOSPACE); value.setSingleLine(); sv.addView(value); row.addView(sv,new LinearLayout.LayoutParams(0,-1,1));
            value.setGravity(Gravity.CENTER_VERTICAL); value.setOnClickListener(v->switchBase(r)); row.setOnClickListener(v->switchBase(r));
            value.setOnLongClickListener(v->{copy(value.getText().toString());return true;});
            row.setContentDescription(names[j]+" 进制"); conversions.addView(row,new LinearLayout.LayoutParams(-1,dp(27))); values.put(r,value);
        }
        LinearLayout actions=new LinearLayout(this);
        Button copy=button("复制",Color.BLACK,12), paste=button("粘贴",Color.BLACK,12);
        copy.setOnClickListener(v->copy(input().isEmpty()?"0":input())); paste.setOnClickListener(v->paste());
        actions.addView(copy,new LinearLayout.LayoutParams(0,dp(32),1)); actions.addView(paste,new LinearLayout.LayoutParams(0,dp(32),1)); root.addView(actions);
        letters=new LinearLayout(this);
        for(char c='A';c<='F';c++) { final String s=""+c; Button key=button(s,0xff252525,19); key.setOnClickListener(v->press(s)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(38),1); lp.setMargins(dp(3),0,dp(3),0); letters.addView(key,lp); keys.put(s,key); }
        root.addView(letters,new LinearLayout.LayoutParams(-1,dp(44)));
        keyboard=new LinearLayout(this); keyboard.setOrientation(LinearLayout.VERTICAL); root.addView(keyboard);
        String[][] layout={{"AC","×","÷","⌫"},{"7","8","9","−"},{"4","5","6","+"},{"1","2","3","%"},{"±","0",".","="}};
        for(String[] line:layout) { LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER);
            for(String s:line) { int color=s.equals("=")?orange:((s.equals("AC")||"×÷⌫−+%".contains(s))?0xff4b4b4d:0xff252527); Button k=button(s,color,s.equals("⌫")?26:32);
                k.setContentDescription(s.equals("⌫")?"删除一位":s.equals("±")?"切换正负":s.equals("AC")?"清空":s.equals(".")?"小数点":s);
                k.setOnClickListener(v->press(s)); if(s.equals("⌫")) k.setOnLongClickListener(v->{input("");fresh=false;update();return true;});
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,0,1); lp.setMargins(dp(4),dp(4),dp(4),dp(4)); row.addView(k,lp); keys.put(s,k);
            } keyboard.addView(row); }
        root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            int size=Math.min((r-l-dp(32))/4, Math.max(dp(35),(b-t-dp(310))/5));
            for(int j=0;j<keyboard.getChildCount();j++){LinearLayout row=(LinearLayout)keyboard.getChildAt(j); for(int k=0;k<4;k++){View key=row.getChildAt(k); LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams)key.getLayoutParams(); if(lp.height!=size-dp(8)){lp.height=size-dp(8);key.setLayoutParams(lp);}}}
        });
        update();
    }
    TextView text(String s,int size,int color) { TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t; }
    Button button(String s,int color,int size) {
        Button b=new Button(this); b.setText(s);b.setTextSize(size);b.setTextColor(white); b.setAllCaps(false);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(0,0,0,0); b.setStateListAnimator(null);
        GradientDrawable g=new GradientDrawable(); g.setColor(color);g.setCornerRadius(dp(100));b.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x55ffffff),g,null)); return b;
    }
    void save() { getSharedPreferences("state",0).edit().putString("decimal",decimal).putString("radix",radix).putBoolean("programmer",programmer).putInt("base",base).putString("history",history).apply(); }
    void switchMode(boolean target) { programmer=target;fresh=false;update(); }
    void switchBase(int target) {
        if(base==target)return;
        try { String result=Calculator.evaluate(radix.isEmpty()?"0":radix,base,true); radix=new BigInteger(result,base).toString(target).toUpperCase(Locale.ROOT);base=target;fresh=false;update(); }
        catch(Exception e){message("请先完成当前表达式");}
    }
    boolean operator(String s) { return s.equals("+")||s.equals("−")||s.equals("×")||s.equals("÷"); }
    void press(String s) {
        String x=input();
        if(s.equals("AC")){ input(""); fresh=false; }
        else if(s.equals("⌫")){ input(x.isEmpty()?"":x.substring(0,x.length()-1));fresh=false; }
        else if(s.equals("=")) {
            if(x.isEmpty()) return;
            try {
                String result=Calculator.evaluate(x,programmer?base:10,programmer);
                if(!result.equals(x)) {
                    history=x+" = "+result+(programmer?"  ["+base+"]":"")+"\n\n"+history;
                    if(history.length()>12000)history=history.substring(0,12000);
                }
                input(result);fresh=true;
            }
            catch(ArithmeticException e) {message("无法计算：除数不能为零或结果超出范围");}
            catch(Exception e){message("请检查表达式");}
        } else if(s.equals("±")) {
            if(x.isEmpty()||x.equals("0")) return;
            String last=x.substring(x.length()-1);
            if(operator(last)||last.equals(".")) return;
            int start=operandStart(x); String n=x.substring(start);
            if(n.startsWith("−")||n.startsWith("-")) input(x.substring(0,start)+n.substring(1));
            else input(x.substring(0,start)+"−"+n);
            fresh=false;
        } else if(s.equals("%")) {
            if(programmer||x.isEmpty()) return;
            String last=x.substring(x.length()-1);
            if(operator(last)||last.equals(".")) return;
            if(x.length()>=256){message("最多输入 256 个字符");return;}
            input(x+"%"); fresh=false;
        } else if(operator(s)) {
            if(x.isEmpty())x="0";
            if(x.endsWith("."))x=x.substring(0,x.length()-1);
            while(!x.isEmpty()&&operator(x.substring(x.length()-1)))x=x.substring(0,x.length()-1);
            if(x.isEmpty())x="0";
            if(x.length()>=256){message("最多输入 256 个字符");return;}
            input(x+s);fresh=false;
        } else {
            if(fresh){x="";fresh=false;}
            if(x.endsWith("%")) x+="×";
            if(x.length()>=256){message("最多输入 256 个字符");return;}
            if(s.equals(".")) {
                String n=x.substring(operandStart(x));
                if(n.contains("."))return;
                if(n.isEmpty()||n.equals("−"))x+="0";
            } else {
                String n=x.substring(operandStart(x));
                if(n.equals("0")||n.equals("−0")) x=x.substring(0,x.length()-1);
            }
            input(x+s);
        } update();
    }
    int operandStart(String s) {
        for(int i=s.length()-1;i>=0;i--) {
            char c=s.charAt(i);
            if(c=='+'||c=='×'||c=='÷'||((c=='−'||c=='-')&&i>0&&(Character.digit(s.charAt(i-1),16)>=0||s.charAt(i-1)=='%'))) return i+1;
        }
        return 0;
    }
    void update() {
        basicTab.setTextColor(programmer?muted:orange);radixTab.setTextColor(programmer?orange:muted);
        String text=input().isEmpty()?"0":input();
        expression.setText(text);
        int len=text.length();
        float size=programmer?(len<=14?30:len<=22?24:20):(len<=10?44:len<=16?36:len<=26?28:22);
        expression.setTextSize(size);
        expressionScroll.post(()->expressionScroll.fullScroll(View.FOCUS_DOWN));
        caption.setText(programmer?"进制转换 · 整数 · 除法取整":"基础计算");
        conversions.setVisibility(programmer?View.VISIBLE:View.GONE); letters.setVisibility(programmer?View.VISIBLE:View.INVISIBLE);
        preview.setText(""); preview.setVisibility(programmer?View.GONE:View.VISIBLE);
        try {
            String expr=input().isEmpty()?"0":input();
            if(!programmer && expr.length()>1 && operator(expr.substring(expr.length()-1))) {
                expr=expr.substring(0,expr.length()-1);
            }
            String result=Calculator.evaluate(expr,programmer?base:10,programmer);
            if(programmer) { BigInteger n=new BigInteger(result,base); for(int r:bases) values.get(r).setText(n.toString(r).toUpperCase(Locale.ROOT)); }
            else if(!result.equals(input())&&!input().isEmpty())preview.setText("= "+result);
        } catch(Exception e) { if(programmer)for(TextView v:values.values())v.setText("—"); }
        for(int r:bases) { TextView v=values.get(r); v.setTextColor(r==base?orange:white); ((View)v.getParent().getParent()).setBackgroundColor(r==base?0xff21170f:Color.BLACK); }
        for(Map.Entry<String,Button> entry:keys.entrySet()) {
            String s=entry.getKey(); boolean enabled=true;
            if(s.length()==1&&Character.digit(s.charAt(0),16)>=0)enabled=!programmer?Character.digit(s.charAt(0),16)<10:Character.digit(s.charAt(0),16)<base;
            if(programmer&&(s.equals(".")||s.equals("%")))enabled=false;
            entry.getValue().setEnabled(enabled);entry.getValue().setAlpha(enabled?1f:.23f);
        } save();
    }
    void copy(String s) {((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("计算结果",s));message("已复制");}
    void paste() {
        android.content.ClipboardManager c=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(!c.hasPrimaryClip())return;
        String s=c.getPrimaryClip().getItemAt(0).coerceToText(this).toString().trim().toUpperCase(Locale.ROOT);
        if(s.length()>256){message("粘贴内容过长");return;}
        try {if(programmer){input(new BigInteger(s,base).toString(base).toUpperCase(Locale.ROOT));}else{input(new BigDecimal(s).stripTrailingZeros().toPlainString());}fresh=false;update();}catch(Exception e){message("请粘贴当前进制的有效数字");}
    }
    void message(String s) {Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
