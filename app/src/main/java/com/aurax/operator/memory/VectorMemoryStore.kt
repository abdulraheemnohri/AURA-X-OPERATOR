package com.aurax.operator.memory

import com.aurax.operator.data.database.AuraDao
import com.aurax.operator.data.entities.MemoryEntity
import kotlin.math.sqrt

data class MemoryMatch(val memory: MemoryEntity, val score: Float)
class VectorMemoryStore(private val dao: AuraDao) {
    private fun vector(text:String):FloatArray { val out=FloatArray(64); text.lowercase().split(' ','\n','\t').filter{it.isNotBlank()}.forEach{token->var h=17;token.forEach{h=h*31+it.code};out[(h and Int.MAX_VALUE)%out.size]+=1f};return out }
    private fun cosine(a:FloatArray,b:FloatArray):Float { var dot=0f;var aa=0f;var bb=0f;for(i in a.indices){dot+=a[i]*b[i];aa+=a[i]*a[i];bb+=b[i]*b[i]};return if(aa==0f||bb==0f)0f else dot/(sqrt(aa)*sqrt(bb)) }
    suspend fun search(query:String,topK:Int=5):List<MemoryMatch> { val q=vector(query);return dao.memories().map{MemoryMatch(it,cosine(q,vector(it.key+" "+it.value)))}.sortedByDescending{it.score}.take(topK) }
    suspend fun add(key:String,value:String):MemoryEntity { val item=MemoryEntity(key=key,value=value);dao.addMemory(item);return item }
}
