package hunoia.sideleap.system.api

import androidx.annotation.StringRes
import hunoia.sideleap.system.feedback.showComposeToast as feedbackShowComposeToast
import hunoia.sideleap.system.feedback.showToast as feedbackShowToast
import hunoia.sideleap.system.feedback.showToastLong as feedbackShowToastLong
import hunoia.sideleap.system.feedback.ToastDuration

fun showToast(text: String) = feedbackShowToast(text)

fun showToast(@StringRes resId: Int) = feedbackShowToast(resId)

fun showToastLong(text: String) = feedbackShowToastLong(text)

fun showToastLong(@StringRes resId: Int) = feedbackShowToastLong(resId)

fun showComposeToast(@StringRes resId: Int, duration: ToastDuration = ToastDuration.Short) =
    feedbackShowComposeToast(resId, duration)

fun showComposeToast(text: String, duration: ToastDuration = ToastDuration.Short) =
    feedbackShowComposeToast(text, duration)
