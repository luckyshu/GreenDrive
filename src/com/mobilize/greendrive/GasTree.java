package com.mobilize.greendrive;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GasTree extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gastree);
        
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Compensation");
		actionBar.setHomeAction(new IntentAction(this, HomeActivity
				.createIntent(this), R.drawable.ic_title_home_default));
		actionBar.setDisplayHomeAsUpEnabled(true);
        
        Button noButton = (Button)findViewById(R.id.no);
    	Button yesButton = (Button)findViewById(R.id.yes);
    	
        TextView tv_gas = (TextView)findViewById(R.id.gasnum);
        TextView tv_tree = (TextView)findViewById(R.id.treenum);
        
        StringBuilder sb_gas = new StringBuilder();        
        StringBuilder sb_tree = new StringBuilder();
        
        Bundle b_gas = getIntent().getExtras();       
        Bundle b_money = getIntent().getExtras();
        if (b_gas != null && b_money != null)
        {
        	String gas = b_gas.getString("gas");
        	String money = b_money.getString("tree");
        	sb_gas.append(Double.valueOf(gas));
     		sb_tree.append(money);
    		tv_gas.setText(sb_gas);
    		tv_tree.setText(sb_tree);
    		yesButton.setText("Donate $" + money +"!");
        }
     

    	
    	noButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
				startActivity(intent);
			}
		});
    	
    	yesButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Context context = getApplicationContext();
				CharSequence text = "Thanks for your support!";
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, text, duration);
				toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 0);
				toast.show();
				Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
				startActivity(intent);
			}
		});
	}
}