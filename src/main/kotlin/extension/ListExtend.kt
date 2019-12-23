package de.hska.flug.extension

import java.util.Random

fun <E> List<E>.random(): E? = if (size > 0) get(Random().nextInt(size)) else null
