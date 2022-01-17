import numpy as np
import tensorflow.compat.v2 as tf
import tensorflow_probability as tfp
from RoomSimulator import RoomSimulator

tfb = tfp.bijectors
tfd = tfp.distributions
psd_kernels = tfp.math.psd_kernels

rs = RoomSimulator(2, 2, observations_at_each_point=1)
rs.simulate()
observation_index_points, observations = rs.get_ap_data(0)

# Define a kernel with trainable parameters. Note we use TransformedVariable
# to apply a positivity constraint.
amplitude = tfp.util.TransformedVariable(
  1., tfb.Exp(), dtype=tf.float64, name='amplitude')
length_scale = tfp.util.TransformedVariable(
  1., tfb.Exp(), dtype=tf.float64, name='length_scale')
kernel = psd_kernels.ExponentiatedQuadratic(amplitude, length_scale, feature_ndims=2)

observation_noise_variance = tfp.util.TransformedVariable(
    np.exp(-5), tfb.Exp(), name='observation_noise_variance')

# We'll use an unconditioned GP to train the kernel parameters.
gp = tfd.GaussianProcess(
    kernel=kernel,
    index_points=observation_index_points,
    observation_noise_variance=observation_noise_variance)

optimizer = tf.optimizers.Adam(learning_rate=.05, beta_1=.5, beta_2=.99)

@tf.function
def optimize():
  with tf.GradientTape() as tape:
    loss = -gp.log_prob(observations)
  grads = tape.gradient(loss, gp.trainable_variables)
  optimizer.apply_gradients(zip(grads, gp.trainable_variables))
  return loss

# We can construct the posterior at a new set of `index_points` using the same
# kernel (with the same parameters, which we'll optimize below).
index_points = [(0,0), (0,1), (1,0), (1,1)]
gprm = tfd.GaussianProcessRegressionModel(
    kernel=kernel,
    index_points=index_points,
    observation_index_points=observation_index_points,
    observations=observations,
    observation_noise_variance=observation_noise_variance)

# # First train the model, then draw and plot posterior samples.
# for i in range(1000):
#   neg_log_likelihood_ = optimize()
#   if i % 100 == 0:
#     print("Step {}: NLL = {}".format(i, neg_log_likelihood_))

# print("Final NLL = {}".format(neg_log_likelihood_))

# samples = gprm.sample(5).numpy()
# print(samples)

# print("Mean Vector:")
# print(gprm.mean())
# print("Standard Deviation Vector:")
# print(gprm.stddev())