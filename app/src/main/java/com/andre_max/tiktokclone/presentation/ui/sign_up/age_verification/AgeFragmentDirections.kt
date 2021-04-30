package com.andre_max.tiktokclone.presentation.ui.sign_up.age_verification

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.andre_max.tiktokclone.R

public class AgeFragmentDirections private constructor() {
  public companion object {
    public fun actionAgeFragmentToBasicSignUpFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_ageFragment_to_basicSignUpFragment)
  }
}
