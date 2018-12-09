package com.connexta.karaf.plugins

import org.apache.karaf.features.FeaturesService
import org.apache.karaf.shell.api.action.Action
import org.apache.karaf.shell.api.action.lifecycle.Reference
import org.jetbrains.annotations.TestOnly

abstract class BaseFeatureCommand : Action {
    @Reference
    protected lateinit var featuresService: FeaturesService

    @TestOnly
    internal fun setFeaturesService(featuresService: FeaturesService) {
        this.featuresService = featuresService
    }

    protected val featureCache by lazy { FeatureCache(featuresService) }
}