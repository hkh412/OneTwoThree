package com.hkh.ott123.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.hkh.ott123.R;

public class LoginPreference extends DialogPreference {

	private Context mContext;
	private String mCurrentValue;
	private String DEFAULT_VALUE = "|";
	
	public LoginPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setDialogLayoutResource(R.layout.dialog_login);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        
        setDialogIcon(null);
	}

	@Override
	protected void onClick() {
		super.onClick();
	}

	@Override
	public CharSequence getTitle() {
//		persistString("rhkdgh412");
		String login = this.getPersistedString(DEFAULT_VALUE);
		if (login == null) {
			return super.getTitle();
		} else {
			return "["+login+"]"+" 로그인 됨";
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return super.onGetDefaultValue(a, index);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if (restorePersistedValue) {
	        // Restore existing state
	        mCurrentValue = this.getPersistedString(DEFAULT_VALUE);
	    } else {
	        // Set default state from the XML attribute
	        mCurrentValue = (String) defaultValue;
	        persistString(mCurrentValue);
	    }
	}
}
