/*
 * @author Muhammad Ubaid Ur Raheem Ahmad AKA Shahbaz Haroon
 * Email: shahbazhrn@gmail.com
 * Cell: +923002585925
 * GitHub: https://github.com/ShahbazHaroon
 */

package com.ubaidsample.docker.util;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MapperUtil {

    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setPropertyCondition(Conditions.isNotNull())
                .setAmbiguityIgnored(true);
    }

    private MapperUtil() {
    }

    public static <S, D> D map(final S source, Class<D> destination) {
        return modelMapper.map(source, destination);
    }

    public static <S, D> void map(final S source, final D destination) {
        modelMapper.map(source, destination);
    }

    public static <S, D> List<D> mapAll(final Collection<S> source, Class<D> destination) {
        return source.stream().map(m -> map(m, destination)).collect(Collectors.toList());
    }

    public static <S, D> Set<D> mapAll(final Set<S> source, Class<D> destination) {
        return source.stream().map(m -> map(m, destination)).collect(Collectors.toSet());
    }
}