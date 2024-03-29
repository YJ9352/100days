package com.example.days.global.infra.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
class RedisConnection(
    @Value("\${spring.data.redis.host}")
    private val host: String,
    @Value("\${spring.data.redis.port}")
    private val port: Int,
    @Value("\${spring.data.redis.password}")
    private val password: String
) {
    @Bean
    fun lettuceConnectionFactory() = RedisStandaloneConfiguration().let {
            it.hostName = host
            it.port = port
            it.setPassword(password)
            it
        }.let { LettuceConnectionFactory(it) }
    
    @Bean
    fun redisCacheManager(): RedisCacheManager{
        return RedisCacheManager.builder(lettuceConnectionFactory())
            .cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
            )
            .build()
    }


    @Bean
    fun redisTemplate(): RedisTemplate<*,*>{
        val genericJackson2JsonRedisSerializer = GenericJackson2JsonRedisSerializer()
        return RedisTemplate<Any, Any>().apply{
            this.connectionFactory = lettuceConnectionFactory()
            this.keySerializer = StringRedisSerializer()
            this.valueSerializer = genericJackson2JsonRedisSerializer
            this.hashKeySerializer = StringRedisSerializer()
            this.hashValueSerializer = genericJackson2JsonRedisSerializer
        }
    }
}