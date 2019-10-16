## Requirements:
* Evaluating expression should automatically recognize opportunity for multiexponentiation and
    use efficient multiexponentiation algorithm.
* More optimization can be done in precompute. Doing too much optimization in evaluate may be
    counterproductive.
* Should allow adding future optimizations.
* Different groups allow for different exponentiation algorithms, groups where inversion is easy (like elliptic curves) can use
    a faster signed digit method like wNAF.


## Design:

TODO: Take a look at more example constructions for design ideas.

Sliding window exponentiation needs precomputed group elements. These can be stored in the group. Then we can use
the group as a singleton and other schemes will have access to the precomputation as well. So we need to add a
precompute method in the group that computes and stores exponentiations of the given group element.
Since exponentiation for sliding window always requires precomputation, we should do an automatic precomputation if necessary
when executing exponentiation. So then later exponentiations can reuse those group elements.

So for simple expression exponentiation:
* Evaluate automatically looks whether powers are already precomputed, else it precomputes them and stores them in
        group for later use by other algorithms. Perhaps we can add option to disregard those lookups/store operations if
        you know you will not use the precomputation, perhaps if you only make one exponentiation with that base.
* For precompute, we always do and store the precomputation.

### Multiexponentiation:

There are two methods: simultaneous and interleaved. Simultaneous precomputes combinations of powers, so it gets much
worse for more bases. Its precomputations are also not reusable for different base combinations. Interleaved still precomputes
powers for each specific base. Therefore, its precomputations can be reused for different multiexponentiations and there
are overall less precomputations. However, simultaneous – if its disatvantages do not matter – is generally faster.

For something like the verification equation of Pointcheval-Sanders 2018, the same bases from the verification key
are reused. So the multiexponentiation will always use the same bases there. This would indicate an advantage for the
simultaneous method. However, the number of bases scales with the length of the message. So for very large messages, the
number of precomputations for the simultaneous method might get too large and then we might want to use the interleaved method.
So one could argue for selecting different methods depending on message length.

`Swante Scholz's recommendation`:
Use simultaneous approach (with window size of 1 or at most 2 because otherwise precomputation consumes too much space) for number of bases not exceeding 10 with caching enabled. 
In all other cases, the interleaving method is better, with a window size of about 4 without caching, and largest window size feasible if caching is enabled.

### Configuration

How do we do configuration. So selection of window size for example. Most granular would be specifying it for each exponentiation.
However, with caching, larger window size is generally better. So most likely, user would a large window size which
does not use too much memory with the group. So perhaps we could put the configuration into the GroupElementExpressionEvaluator.
Not specifying an evaluator would then lead to default values used.
In general, all configuration can be set for the evaluator. Then the evaulator can also be reused.
One problem of this is that the evaluator is group specific. So you could not reuse the configuration for a different group.
So perhaps we need a factory class where we can set configuration and set group and then it gives us evaluator.
Then we can change the group and still reuse our configuration.

Possible things to configure:
* Window size for exponentiation.
* Which exact algorithm to use for exponentiation.
* As discussed above, skipping store/loading of precomputed group elements for one-time exponentiation.
* Whether to use a simultaneous or interleaved method for multiexponentiation. Also the inner exponentiation algorithm.

### Example application in constructions

For [PS18]:
* Computation of verification key requires exponentiating \tilde{g} with many different elements. There, precomputation
    for \tilde{g} is helpful. 
* Precomputation might be helpful for verification during key generation.
* When doing verification, multiexponentiation algorithm should be used.



## Material

Singleton code (thread-safe):
```Java
public class Singleton {

    private SingletonClass() {}

    private static class InstanceHolder {

        static final Singleton INSTANCE = new Singleton();

    }

    public static Singleton getInstance() {

        return InstanceHolder.INSTANCE;

    }

}
```
