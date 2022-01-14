package com.example.mockkdemo

import io.mockk.called
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkConstructor
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import io.mockk.verifySequence
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.util.StopWatch

class MockkTests {
    private val stopWatch = StopWatch()

    private val animal = mockk<Animal>()
    private val manWithAnimal = ManWithAnimal(animal)

    // region -- matchers --

    @Test
    fun testSimpleCall() {
        every { animal.say() } returns "Meow"

        assertThat(manWithAnimal.callAnimal("Kitty"))
            .isEqualTo("Kitty Meow")
    }

    @Test
    fun testCallWithArgs() {
        every { animal.say(any()) } returns "Meooooow"

        assertThat(manWithAnimal.superCallAnimal("Kitty", times = 1))
            .isEqualTo("Kitty Meooooow")
        assertThat(manWithAnimal.superCallAnimal("Kitty", times = 3))
            .isEqualTo("Kitty Meooooow")
    }

    @Test
    fun testCallWithArgs2() {
        every { animal.say(less(4)) } returns "Woof"
        every { animal.say(and(more(3), less(6))) } returns "Meooooow"
        every { animal.say(more(5)) } returns "Graaaa"

        assertThat(manWithAnimal.superCallAnimal("Kitty", times = 2))
            .isEqualTo("Kitty Woof")
        assertThat(manWithAnimal.superCallAnimal("Kitty", times = 5))
            .isEqualTo("Kitty Meooooow")
        assertThat(manWithAnimal.superCallAnimal("Kitty", times = 10))
            .isEqualTo("Kitty Graaaa")
    }

    @Test
    fun testMultipleArgs() {
        // Вот здесь можем миксовать значения и матчеры
        every { animal.sayAndMove(2, any()) } returns "said 2 and moved some"

        assertThat(animal.sayAndMove(2, 2))
            .isEqualTo("said 2 and moved some")
        assertThat(animal.sayAndMove(2, 3))
            .isEqualTo("said 2 and moved some")
    }

    @Test
    fun testAnswer() {
        every { animal.say(any()) } answers { "Meow ${arg<Int>(0)} times" }

        assertThat(animal.say(times = 2))
            .isEqualTo("Meow 2 times")
    }

    @Test
    fun testMultipleCalls() {
        every { animal.say() } returnsMany listOf("Woof 1", "Woof 2", "Woof 3")

        assertThat(animal.say()).isEqualTo("Woof 1")
        assertThat(animal.say()).isEqualTo("Woof 2")
        assertThat(animal.say()).isEqualTo("Woof 3")

        every { animal.say() } returns "Woof 4" andThen "Woof 5" andThen "Woof 6"

        assertThat(animal.say()).isEqualTo("Woof 4")
        assertThat(animal.say()).isEqualTo("Woof 5")
        assertThat(animal.say()).isEqualTo("Woof 6")

    }

    @Test
    fun testImplicitMocking() {
        val manWithFeedableAnimal = ManWithFeedableAnimal(mockk())
        manWithFeedableAnimal.callAndPutAccessory("Sparky", true)
    }

    @Test
    fun testRelaxedMocking() {
        val manWithFeedableAnimal = ManWithFeedableAnimal(mockk(relaxed = true))
        manWithFeedableAnimal.callAndPutAccessory("Sparky", true)
    }

    @Test
    fun testRelaxedUnitFunImplicitMocking() {
        val manWithFeedableAnimal = ManWithFeedableAnimal(mockk(relaxUnitFun = true))
        manWithFeedableAnimal.callAndPutAccessory("Sparky", true)
    }

    @Test
    fun testRelaxedUnitFunMocking() {
        val animal: FeedableAnimal = mockk(relaxUnitFun = true)
        val manWithFeedableAnimal = ManWithFeedableAnimal(animal)
        every { animal.say() } returns ""
        manWithFeedableAnimal.callAndPutAccessory("Sparky", true)
    }

    // endregion

    // region -- additional mocking --

    @Test
    fun testEnum() {
        val animal = FeedableDog(startingEnergy = 15)
        assertThat(animal.hungerStatus()).isEqualTo(HungerStatus.STARVING)

        mockkObject(HungerStatus.FULL)
        mockkObject(HungerStatus.STARVING)
        every { HungerStatus.FULL.lowerBound } returns 1
        every { HungerStatus.FULL.upperBound } returns 20
        every { HungerStatus.STARVING.lowerBound } returns 60
        every { HungerStatus.STARVING.upperBound } returns 100

        assertThat(animal.hungerStatus()).isEqualTo(HungerStatus.FULL)
    }

    @Test
    fun testConstructor() {
        mockkConstructor(FeedableDog::class)

        every { anyConstructed<FeedableDog>().say() } returns "I'm a cyberdog"

        assertThat(FeedableDog().say()).isEqualTo("I'm a cyberdog")

        unmockkConstructor(FeedableDog::class)
    }

    @Test
    fun testPrivateMethod() {
        val mockDog = spyk<FeedableDog>(recordPrivateCalls = true)

        assertThat(mockDog.move(1)).isEqualTo("Walked 1 meters")
        assertThat(mockDog.move(1)).isEqualTo("I won't do it")
        assertThat(mockDog.move(1)).isEqualTo("I won't do it")

        // Option 1
        every { mockDog invoke "spendEnergy" withArguments listOf(any<Int>()) } returns true
        assertThat(mockDog.move(1)).isEqualTo("Walked 1 meters")
        assertThat(mockDog.move(1)).isEqualTo("Walked 1 meters")

        clearMocks(mockDog)
        // Option 2
        every { mockDog["spendEnergy"](any<Int>()) } returns true
        assertThat(mockDog.move(1)).isEqualTo("Walked 1 meters")
        assertThat(mockDog.move(1)).isEqualTo("Walked 1 meters")
    }

    @Test
    fun testStaticMockJava() {
        assertThat(JavaKennel.newDog().say()).isEqualTo("Bark")

        stopWatch.start()
        mockkStatic(JavaKennel::class)
        every { JavaKennel.newDog() } returns FeedableDog()
        assertThat(JavaKennel.newDog().say()).isEqualTo("I'm feedable dog. Bark.")
        unmockkStatic(JavaKennel::class)
        stopWatch.stop()
        println(stopWatch.totalTimeMillis)

        assertThat(JavaKennel.newDog().say()).isEqualTo("Bark")
    }

    @Test
    fun testStaticMockKotlin() {
        assertThat(Kennel.newDog().say()).isEqualTo("Bark")

        stopWatch.start()
        mockkObject(Kennel)
        every { Kennel.Companion.newDog() } returns FeedableDog()
        assertThat(Kennel.newDog().say()).isEqualTo("I'm feedable dog. Bark.")
        unmockkObject(Kennel)
        stopWatch.stop()
        println(stopWatch.totalTimeMillis)

        assertThat(Kennel.newDog().say()).isEqualTo("Bark")
    }

    // endregion

    // region - Capturing -

    @Test
    fun testCaptureArg() {
        val dog = mockk<Dog>()

        val slot = slot<Int>()
        every { dog.say(capture(slot)) } returns ""
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)

        assertThat(slot.captured).isEqualTo(5)
    }

    @Test
    fun testMultipleCaptureArg() {
        val dog = mockk<Dog>()

        val list = mutableListOf<Int>()
        every { dog.say(capture(list)) } returns ""
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)
        manWithAnimal.superCallAnimal("Sparky", 6)
        manWithAnimal.superCallAnimal("Sparky", 7)

        assertThat(list[0]).isEqualTo(5)
        assertThat(list[1]).isEqualTo(6)
        assertThat(list[2]).isEqualTo(7)
    }

    // endregion

    // region - verification -

    @Test
    fun testSimpleVerification() {
        val dog = mockk<Dog>()
        every { dog.say(any()) } returns ""
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)

        verify { dog.say(any()) }
    }

    @Test
    fun testManyCallsVerification() {
        val dog = mockk<Dog>()
        every { dog.say(any()) } returns ""
        val manWithAnimal = ManWithAnimal(dog)
        manWithAnimal.superCallAnimal("Sparky", 5)
        manWithAnimal.superCallAnimal("Sparky", 50)
        manWithAnimal.superCallAnimal("Sparky", 500)

        verify(exactly = 3) { dog.say(any()) }
    }

    @Test
    fun testZeroCallsVerification() {
        val dog1 = mockk<Dog>()
        val dog2 = mockk<Dog>()
        every { dog1.say(any()) } returns ""
        every { dog2.say(any()) } returns ""
        val manWithAnimal = ManWithAnimal(dog1)
        manWithAnimal.superCallAnimal("Sparky", 5)
        manWithAnimal.superCallAnimal("Sparky", 50)
        manWithAnimal.superCallAnimal("Sparky", 500)

        verify{ dog2 wasNot called }
    }


    @Test
    fun testVerifyInOrder() {
        val dog1 = mockk<Dog>()
        val dog2 = mockk<Dog>()
        every { dog1.say(any()) } returns ""
        every { dog2.say(any()) } returns ""
        val manWithAnimal1 = ManWithAnimal(dog1)
        val manWithAnimal2 = ManWithAnimal(dog2)

        manWithAnimal1.superCallAnimal("Sparky", 2)
        manWithAnimal2.superCallAnimal("Charlie", 3)

        verifyOrder {
            dog1.say(any())
            dog2.say(any())
        }
    }

    @Test
    fun testVerifySequence() {
        every { animal.say(any()) } returns "Meow"

        manWithAnimal.superCallAnimal("Sparky", 1)
        manWithAnimal.superCallAnimal("Sparky", 2)
        manWithAnimal.superCallAnimal("Charlie", 3)

        verifySequence {
            animal.say(1)
//            animal.say(2)
            animal.say(3)
        }
    }

    @Test
    fun testVerifyConfirmed() {
        val dog1 = mockk<Dog>()
        val dog2 = mockk<Dog>()
        every { dog1.say(any()) } returns ""
        every { dog2.say(any()) } returns ""
        val manWithAnimal1 = ManWithAnimal(dog1)
        val manWithAnimal2 = ManWithAnimal(dog2)

        manWithAnimal1.superCallAnimal("Sparky", 2)
        manWithAnimal2.superCallAnimal("Charlie", 3)

        verify {
            dog1.say(any())
        }

//        confirmVerified(dog1, dog2)
    }



    // endregion
}
