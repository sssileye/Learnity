package com.miage.learnity.model

class VirtualDebtModel {
    fun getAbsencePenalty(redevance: Double): Double = redevance
    fun getErrorPenalty(redevance: Double): Double = redevance / 10.0
}