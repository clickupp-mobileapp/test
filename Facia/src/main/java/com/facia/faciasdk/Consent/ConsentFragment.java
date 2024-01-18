package com.facia.faciasdk.Consent;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.facia.faciasdk.Activity.Helpers.Enums.ServiceType;
import com.facia.faciasdk.Activity.Helpers.IntentHelper;
import com.facia.faciasdk.Activity.Helpers.RequestModel;
import com.facia.faciasdk.DocumentType.DocTypeFragment;
import com.facia.faciasdk.R;
import com.facia.faciasdk.Logs.Webhooks;
import com.facia.faciasdk.Singleton.SingletonData;
import com.facia.faciasdk.Utils.Constants.ApiConstants;
import com.facia.faciasdk.Utils.Constants.TimeConstants;
import com.facia.faciasdk.databinding.FragmentConsentBinding;

import java.util.HashMap;

public class ConsentFragment extends Fragment implements View.OnClickListener {
    protected Boolean isConsentChecked = false;
    private RequestModel requestModel;
    /**
     * to handle screen's back press click
     */
    OnBackPressedCallback backPressCallBack = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            try {
                exitDialog();
            } catch (Exception e) {
                Webhooks.exceptionReport(e, "ConsentFragment/handleOnBackPressed");
            }
        }
    };
    private FragmentConsentBinding fragmentConsentBinding;
    private ClickListeners clickListeners;

    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentConsentBinding = FragmentConsentBinding.inflate(inflater, container, false);
        initialization();
        return fragmentConsentBinding.getRoot();
    }

    /**
     * method to initialize instances and values
     * init callback for back press
     * invoking method for assigning click listeners
     */
    private void initialization() {
        try {
            requestModel = (RequestModel) IntentHelper.getInstance().getObject(ApiConstants.REQUEST_MODEL);
//            setConsentCheckStr();
            fragmentConsentBinding.iAgreeTxt.setMovementMethod(LinkMovementMethod.getInstance());
            fragmentConsentBinding.iAgreeCheckBox.setMovementMethod(LinkMovementMethod.getInstance());
            clickListeners = new ClickListeners(fragmentConsentBinding, this);
            fragmentConsentBinding.iAgreeCheckBox.setChecked(false);
            isConsentChecked = false;
            requireActivity().getOnBackPressedDispatcher().addCallback(this.requireActivity(), backPressCallBack);
            assignClickListeners();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "ConsentFragment/initialization");
        }
    }

    /**
     * assigning click listeners of ui elements
     */
    private void assignClickListeners() {
        try {
            fragmentConsentBinding.continueBtn.setOnClickListener(this);
            fragmentConsentBinding.iAgreeCheckBox.setOnClickListener(this);
            fragmentConsentBinding.iAgreeTxt.setOnClickListener(this);
            fragmentConsentBinding.consentAgreeParent.setOnClickListener(this);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "ConsentFragment/assignClickListeners");
        }
    }

    /**
     * callback for UI clicks
     *
     * @param view view on which user clicked
     */
    @Override
    public void onClick(View view) {
        try {
            synchronized (view) {
                view.setEnabled(false);
                int id = view.getId();
                if (id == R.id.continueBtn) {
                    clickListeners.handleContinueClick(!requestModel.isSimilarity() && requestModel.getConfigObject()
                                    .getBoolean("showVerificationType"), ServiceType.valueOf(
                                            requestModel.getConfigObject().getString("serviceType")) == ServiceType.DOCUMENT_LIVENESS &&
                            requestModel.getConfigObject().getBoolean("showDocumentType"), requestModel.isSimilarity());
                } else if (id == R.id.iAgreeCheckBox) {
                    clickListeners.handleConsentCheck();
                }  else if (id == R.id.iAgreeTxt || id == R.id.consentAgreeParent) {
                    if (fragmentConsentBinding.iAgreeCheckBox.isChecked()){
                        fragmentConsentBinding.iAgreeCheckBox.setChecked(false);
                    }else {
                        fragmentConsentBinding.iAgreeCheckBox.setChecked(true);
                    }
                    clickListeners.handleConsentCheck();
                }
                new Handler().postDelayed(() -> view.setEnabled(true), TimeConstants.SYNCHRONIZED_CONSTANT);
            }
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "ConsentFragment/onClick");
        }
    }

    /**
     * method to show exit Dialog on back press
     */
    private void exitDialog() {
        try {
            Dialog dialog = new Dialog(SingletonData.getInstance().getContext());
            dialog.setContentView(R.layout.exit_dialog_box);
            dialog.setCancelable(false);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            Button negativeButton = dialog.findViewById(R.id.reject);
            Button positiveButton = dialog.findViewById(R.id.accept);
            negativeButton.setOnClickListener(view -> dialog.dismiss());
            positiveButton.setOnClickListener(view -> terminateSDK());
            dialog.show();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "ConsentFragment/exitDialog");
        }
    }

    /**
     * method to terminate sdk in case of back press (request cancellation)
     */
    private void terminateSDK() {
        try {
            HashMap<String, String> requestResponseObj = new HashMap<>();
            requestResponseObj.put("reference_id", "");
            requestResponseObj.put("event", "request.cancelled");
            requestModel.getRequestListener().requestStatus(requestResponseObj);
            SingletonData.getInstance().getActivity().finish();
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "ConsentFragment/terminateSDK");
        }
    }


    /**
     * making parts og string to be clickable
     * to redirect to the particular links
     */
    private void setConsentCheckStr(){
        try{
//            SpannableString consentCheckStr = new SpannableString(getString(R.string.i_agree));
//            ClickableSpan privacyClickableSpan = new ClickableSpan() {
//                @Override
//                public void onClick(View textView) {
//                    Uri uri = Uri.parse("https://facia.ai/privacy-policy/");
//                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                    startActivity(intent);
//                }
//                @Override
//                public void updateDrawState(TextPaint ds) {
//                    super.updateDrawState(ds);
//                    ds.setUnderlineText(true);
//                }
//            };
//            ClickableSpan termsClickableSpan = new ClickableSpan() {
//                @Override
//                public void onClick(View textView) {
//                    Uri uri = Uri.parse("https://facia.ai/terms-service/");
//                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                    startActivity(intent);
//                }
//                @Override
//                public void updateDrawState(TextPaint ds) {
//                    super.updateDrawState(ds);
//                    ds.setUnderlineText(true);
//                }
//            };
//            consentCheckStr.setSpan(privacyClickableSpan, 50, 64, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            consentCheckStr.setSpan(termsClickableSpan, 67, 79, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            fragmentConsentBinding.iAgreeTxt.setText(consentCheckStr);
        } catch (Exception e) {
            Webhooks.exceptionReport(e, "ConsentFragment/setConsentCheckStr");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
