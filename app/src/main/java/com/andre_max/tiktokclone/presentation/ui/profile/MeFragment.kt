package com.andre_max.tiktokclone.presentation.ui.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.andre_max.tiktokclone.R
import com.andre_max.tiktokclone.databinding.FragmentMeBinding
import com.andre_max.tiktokclone.presentation.ui.profile.with_account.ProfileWithAccountFragment
import com.andre_max.tiktokclone.presentation.ui.profile.without_account.ProfileWithoutAccountFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MeFragment : Fragment(R.layout.fragment_me) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().supportFragmentManager.beginTransaction()
            .add(
                R.id.frag_container,
                if (Firebase.auth.currentUser == null) ProfileWithoutAccountFragment() else ProfileWithAccountFragment()
            )
            .commit()
    }
}
