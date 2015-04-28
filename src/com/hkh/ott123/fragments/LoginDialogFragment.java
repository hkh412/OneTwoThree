package com.hkh.ott123.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hkh.ott123.R;
import com.hkh.ott123.config.Links;
import com.hkh.ott123.data.Session;
import com.hkh.ott123.manager.AppDataManager;
import com.hkh.ott123.manager.LoginManager;
import com.hkh.ott123.manager.SessionManager;
import com.hkh.ott123.manager.SharedPreferenceManager;

public class LoginDialogFragment extends DialogFragment {

	protected static final String TAG = LoginDialogFragment.class.getSimpleName();
	Context mContext;
	LoginListener mListener;
	EditText etUsername;
	EditText etPassword;
	TextView tvNickname;
	TextView tvLevel;
	TextView tvPoint;
	TextView tvMemberJoin;
	boolean hasSession = false;
	
	public interface LoginListener {
		void onLoginComplete();
		void onLoginFailure(String message);
		void onLogoutComplete();
		void onLogoutFailure();
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mContext = getActivity();
		
	    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	    
	    // 로그인 상태 판단 (로그인/로그아웃) 결정
	    View loginView;
	    int textId;
	    SessionManager manager = SessionManager.getInstance();
	    Session session = manager.getSession();
	    hasSession = session != null;
	    if (hasSession) {
	    	// 로그인 됨 / 로그아웃 대화창 열림
	    	loginView = inflater.inflate(R.layout.dialog_logout, null);
	    	textId = R.string.logout;
	    	tvNickname = (TextView) loginView.findViewById(R.id.tv_profile_nickname);
			tvLevel = (TextView) loginView.findViewById(R.id.tv_profile_level);
			tvPoint = (TextView) loginView.findViewById(R.id.tv_profile_point);
			tvNickname.setText(session.getNickName());
			tvLevel.setText(session.getLevel());
			tvPoint.setText(session.getPoint());
			
	    } else {
	    	loginView = inflater.inflate(R.layout.dialog_login, null);
	    	textId = R.string.login;
		    etUsername = (EditText)loginView.findViewById(R.id.edit_username);
		    etPassword = (EditText)loginView.findViewById(R.id.edit_password);
		    tvMemberJoin = (TextView)loginView.findViewById(R.id.tv_member_join);
		    tvMemberJoin.setText(Html.fromHtml(mContext.getString(R.string.text_member_join)));
		    tvMemberJoin.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						String domainUrl = AppDataManager.getInstance().getCurrentCityData().getUrl();
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(domainUrl+Links.MEMBER_JOIN));
						mContext.startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			});
		    
		    // 저장된 아이디 비밀번호가 있으면 셋팅한다.
		    SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		    String userName = spm.getString("username");
		    String passWord = spm.getString("password");
		    if (userName != null) {
		    	etUsername.setText(userName);
		    }
		    if (passWord != null) {
		    	etPassword.setText(passWord);
		    }
	    }
	    
	    builder.setView(loginView)
	    .setPositiveButton(textId, new DialogInterface.OnClickListener() {
	    	@Override
	    	public void onClick(DialogInterface dialog, int id) {
	    		if (hasSession) {
	    			onLogoutAction();
	    		} else {
	    			onLoginAction(etUsername.getText(), etPassword.getText());
	    		}
	    	}
	    })
	    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		LoginDialogFragment.this.getDialog().cancel();
	    	}
	    });      
	    return builder.create();
	}
	
	public void setLoginListener(LoginListener listener) {
		mListener = listener;
	}
	/**
	 * 로그인
	 */
	private void onLoginAction(CharSequence username, CharSequence password) {
		String userName = username.toString().trim();
		String passWord = password.toString().trim();
		SharedPreferenceManager manager = SharedPreferenceManager.getInstance(mContext);
		manager.putString("username", userName);
		manager.putString("password", passWord);
		LoginManager.getInstance().doLogin(mContext, userName, passWord, mListener);
	}
	
	/**
	 * 로그아웃
	 */
	private void onLogoutAction() {
		// Session 지움
		LoginManager.getInstance().doLogout(mContext, mListener);
	}
}
