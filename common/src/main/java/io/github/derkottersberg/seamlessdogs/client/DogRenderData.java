package io.github.derkottersberg.seamlessdogs.client;

/** Extra extracted data lives on each render state, never on a shared model. */
public interface DogRenderData {
    PetAnimation.Sample seamlessdogs$sample();
    void seamlessdogs$sample(PetAnimation.Sample sample);
}
