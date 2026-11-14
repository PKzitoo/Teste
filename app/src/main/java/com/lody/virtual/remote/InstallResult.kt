package com.lody.virtual.remote

data class InstallResult(
    val error: String? = null,
    val isSuccess: Boolean = error == null
)
