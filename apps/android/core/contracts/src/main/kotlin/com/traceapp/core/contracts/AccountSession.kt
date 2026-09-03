package com.traceapp.core.contracts

/** Supplies the account currently using this on-device installation. */
interface AccountSession {
    fun currentAccountId(): String?
}
