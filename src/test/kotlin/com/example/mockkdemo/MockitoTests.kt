package com.example.mockkdemo

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito
import org.springframework.util.StopWatch


class MockitoTests {

    private val stopWatch = StopWatch()
    private val animal = mock<Animal>()
    private val manWithAnimal = ManWithAnimal(animal)

    // region -- matchers --

    @Test
    fun testSimpleCall() {
        whenever(animal.say())
            .thenReturn("Meow")

        assertThat(manWithAnimal.callAnimal("Kitty"))
            .isEqualTo("Kitty Meow")
    }

    @Test
    fun testCallWithArgs() {
        whenever(animal.say(any()))
            .thenReturn("Meooooow")

        assertThat(manWithAnimal.superCallAnimal("Kitty", times = 1))
            .isEqualTo("Kitty Meooooow")
        assertThat(manWithAnimal.superCallAnimal("Kitty", times = 3))
            .isEqualTo("Kitty Meooooow")
    }

    @Test
    fun testMultipleArgs() {
        // Вот здесь можем оперировать только матчерами, миксовать значения и матчеры НЕЛЬЗЯ!!
        whenever(animal.sayAndMove(eq(2), any()))
            .thenReturn("said 2 and moved some")

        assertThat(animal.sayAndMove(2, 2))
            .isEqualTo("said 2 and moved some")
        assertThat(animal.sayAndMove(2, 3))
            .isEqualTo("said 2 and moved some")
    }

    @Test
    fun testAnswer() {
        whenever(animal.say(any()))
            .thenAnswer {
                "Meow ${it.getArgument<Int>(0)} times"
            }

        assertThat(animal.say(times = 2))
            .isEqualTo("Meow 2 times")
    }

    @Test
    fun testMultipleCalls() {
        whenever(animal.say()).thenReturn("Woof 1", "Woof 2", "Woof 3")

        assertThat(animal.say()).isEqualTo("Woof 1")
        assertThat(animal.say()).isEqualTo("Woof 2")
        assertThat(animal.say()).isEqualTo("Woof 3")
    }

    @Test
    fun testImplicitMocking() {
        val manWithFeedableAnimal = ManWithFeedableAnimal(mock())
        manWithFeedableAnimal.callAndPutAccessory("Sparky", true)
    }

    // endregion

    @Test
    fun testStaticMock() {
        assertThat(JavaKennel.newDog().say()).isEqualTo("Bark")

        stopWatch.start()
        Mockito.mockStatic(JavaKennel::class.java).use { mockedStatic ->
            mockedStatic.`when`<Any>(JavaKennel::newDog).thenReturn(FeedableDog())
            assertThat(JavaKennel.newDog().say()).isEqualTo("I'm feedable dog. Bark.")
        }
        stopWatch.stop()
        println(stopWatch.totalTimeMillis)

        assertThat(JavaKennel.newDog().say()).isEqualTo("Bark")
    }

    // No PowerMock for junit5

    // region - Capturing -

    @Test
    fun testCaptureArg() {
        val dog = mock<Dog>()
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)

        val capture = argumentCaptor<Int> {
            verify(dog).say(capture())
            assertThat(firstValue).isEqualTo(5)
        }
    }

    @Test
    fun testMultipleCaptureArg() {
        val dog = mock<Dog>()
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)
        manWithAnimal.superCallAnimal("Sparky", 6)
        manWithAnimal.superCallAnimal("Sparky", 7)

        val capture = argumentCaptor<Int> {
            verify(dog, times(3)).say(capture())
            assertThat(firstValue).isEqualTo(5)
            assertThat(secondValue).isEqualTo(6)
            assertThat(thirdValue).isEqualTo(7)
        }
    }

    // endregion

    // region - verification -

    @Test
    fun testSimpleVerification() {
        val dog = mock<Dog>()
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)

        verify(dog).say(any())
    }

    @Test
    fun testManyCallsVerification() {
        val dog = mock<Dog>()
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)
        manWithAnimal.superCallAnimal("Sparky", 50)
        manWithAnimal.superCallAnimal("Sparky", 500)

        verify(dog, times(3)).say(any())
    }

    @Test
    fun testZeroCallsVerification() {
        val dog1 = mock<Dog>()
        val dog2 = mock<Dog>()
        val manWithAnimal = ManWithAnimal(dog1)
        manWithAnimal.superCallAnimal("Sparky", 5)
        manWithAnimal.superCallAnimal("Sparky", 50)
        manWithAnimal.superCallAnimal("Sparky", 500)


        Mockito.verifyNoInteractions(dog2)
//        verifyZeroInteractions(dog2)
//        com.nhaarman.mockitokotlin2 » mockito-kotlin:2.2.0    Sep, 2019
    }

    @Test
    fun testVerifyInOrder() {
        val dog1 = mock<Dog>()
        val dog2 = mock<Dog>()
        val manWithAnimal1 = ManWithAnimal(dog1)
        val manWithAnimal2 = ManWithAnimal(dog2)
        manWithAnimal1.superCallAnimal("Sparky", 3)
        manWithAnimal2.superCallAnimal("Charlie", 2)

        inOrder(dog1, dog2) {
            verify(dog1).say(any())
            verify(dog2).say(any())
        }
    }

    @Test
    fun testVerifySequence() {
        manWithAnimal.superCallAnimal("Sparky", 1)
        manWithAnimal.superCallAnimal("Sparky", 2)
        manWithAnimal.superCallAnimal("Sparky", 3)

        inOrder(animal) {
            verify(animal).say(1)
            verify(animal).say(3)
        }
    }

    // endregion
}
