package com.sorokinuladzimir.homebarassistant.ui.utils

import com.sorokinuladzimir.homebarassistant.db.entity.Glass

object SpinnerGlassMatcher {

    fun matchGlass(glasses: Array<String>, glass: Glass?): Int =
        if (glass != null) glasses.indexOf(glass.glassName) else 0
}
