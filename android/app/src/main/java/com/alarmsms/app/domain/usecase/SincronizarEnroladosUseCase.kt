package com.alarmsms.app.domain.usecase

import com.alarmsms.app.domain.repository.EnroladoRepository
import com.alarmsms.app.domain.repository.ConfigRepository
import javax.inject.Inject

class SincronizarEnroladosUseCase @Inject constructor(
    private val enroladoRepo: EnroladoRepository,
    private val configRepo: ConfigRepository
) {
    suspend operator fun invoke() {
        // Sync of global configs
        configRepo.synchroniseConfigWithFirestore()
        
        // Sync of of grid enrolment registry
        enroladoRepo.synchroniseWithFirestore()
    }
}
