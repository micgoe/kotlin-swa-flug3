package de.hska.flug

import de.hska.flug.config.Settings.banner
import de.hska.flug.config.Settings.props
import org.springframework.boot.WebApplicationType.REACTIVE
import org.springframework.boot.actuate.autoconfigure.cache.CachesEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.condition.ConditionsReportEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.logging.LoggersEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.management.HeapDumpWebEndpointAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.LogbackMetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.SystemMetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.metrics.web.tomcat.TomcatMetricsAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.scheduling.ScheduledTasksEndpointAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration
import org.springframework.boot.autoconfigure.hateoas.HypermediaAutoConfiguration
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.runApplication
import org.springframework.cloud.autoconfigure.LifecycleMvcEndpointAutoConfiguration
import org.springframework.cloud.autoconfigure.RefreshEndpointAutoConfiguration
import org.springframework.cloud.sleuth.instrument.rxjava.RxJavaAutoConfiguration
import reactor.tools.agent.ReactorDebugAgent

@SpringBootApplication(
    exclude = [
        AopAutoConfiguration::class,
        CachesEndpointAutoConfiguration::class,
        CompositeMeterRegistryAutoConfiguration::class,
        ConditionsReportEndpointAutoConfiguration::class,
        EmbeddedWebServerFactoryCustomizerAutoConfiguration::class,
        ErrorMvcAutoConfiguration::class,
        GsonAutoConfiguration::class,
        HeapDumpWebEndpointAutoConfiguration::class,
        HypermediaAutoConfiguration::class,
        JvmMetricsAutoConfiguration::class,
        KafkaMetricsAutoConfiguration::class,
        LifecycleMvcEndpointAutoConfiguration::class,
        LogbackMetricsAutoConfiguration::class,
        LoggersEndpointAutoConfiguration::class,
        MetricsAutoConfiguration::class,
        MongoRepositoriesAutoConfiguration::class,
        PersistenceExceptionTranslationAutoConfiguration::class,
        RefreshEndpointAutoConfiguration::class,
        RestTemplateAutoConfiguration::class,
        RxJavaAutoConfiguration::class,
        SimpleMetricsExportAutoConfiguration::class,
        ScheduledTasksEndpointAutoConfiguration::class,
        SystemMetricsAutoConfiguration::class,
        TaskExecutionAutoConfiguration::class,
        TaskSchedulingAutoConfiguration::class,
        TomcatMetricsAutoConfiguration::class,
        TransactionAutoConfiguration::class,
        WebMvcAutoConfiguration::class
    ]
)

class Application

fun main(args: Array<String>) {
    ReactorDebugAgent.init()

    @Suppress("SpreadOperator")
    (runApplication<Application>(*args) {
        webApplicationType = REACTIVE
        setBanner(banner)
        setDefaultProperties(props)
        addListeners(ApplicationPidFileWriter())
    })
}
